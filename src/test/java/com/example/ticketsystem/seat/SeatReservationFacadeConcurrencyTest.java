package com.example.ticketsystem.seat;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticketsystem.domain.Concert;
import com.example.ticketsystem.domain.ConcertSchedule;
import com.example.ticketsystem.domain.Seat;
import com.example.ticketsystem.domain.SeatStatus;
import com.example.ticketsystem.domain.User;
import com.example.ticketsystem.repository.ConcertRepository;
import com.example.ticketsystem.repository.ConcertScheduleRepository;
import com.example.ticketsystem.repository.SeatRepository;
import com.example.ticketsystem.repository.UserRepository;
import com.example.ticketsystem.seat.exception.SeatHoldException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SeatReservationFacadeConcurrencyTest {

    private static final int THREAD_COUNT = 100;

    @Autowired
    private SeatReservationFacade seatReservationFacade;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long seatId;
    private List<Long> userIds;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        cleanRedisSeatKeys();

        LocalDateTime now = LocalDateTime.now();
        userIds = userRepository.saveAll(
                IntStream.rangeClosed(1, THREAD_COUNT)
                    .mapToObj(index -> new User("user" + index + "@example.com", "user" + index, now))
                    .toList()
            )
            .stream()
            .map(User::getId)
            .toList();

        Concert concert = concertRepository.save(new Concert("Ticket Concert", "Artist", now));
        ConcertSchedule schedule = concertScheduleRepository.save(
            new ConcertSchedule(concert, now.plusDays(7), now)
        );
        Seat seat = seatRepository.saveAndFlush(
            new Seat(schedule, "A-1", "VIP", BigDecimal.valueOf(100_000), SeatStatus.AVAILABLE)
        );
        seatId = seat.getId();
    }

    private void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        try {
            List.of(
                "PAYMENTS",
                "ORDERS",
                "SEAT_HOLDS",
                "SEATS",
                "CONCERT_SCHEDULES",
                "CONCERTS",
                "USERS",
                "OUTBOX_EVENTS",
                "PROCESSED_EVENTS",
                "SAGA_INSTANCES"
            ).forEach(tableName -> jdbcTemplate.execute("TRUNCATE TABLE " + tableName));
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private void cleanRedisSeatKeys() {
        redissonClient.getKeys().deleteByPattern("seat:*:held");
        redissonClient.getKeys().deleteByPattern("lock:seat:*");
    }

    @Test
    void onlyOneUserCanHoldSameSeatWithConcurrentRequests() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger();
        ConcurrentLinkedQueue<Throwable> unexpectedFailures = new ConcurrentLinkedQueue<>();

        for (Long userId : userIds) {
            executorService.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    seatReservationFacade.holdSeat(seatId, userId);
                    successCount.incrementAndGet();
                } catch (SeatHoldException expectedFailure) {
                    // Expected for losing concurrent requests.
                } catch (Throwable throwable) {
                    unexpectedFailures.add(throwable);
                } finally {
                    done.countDown();
                }
            });
        }

        try {
            boolean allThreadsReady = ready.await(10, TimeUnit.SECONDS);
            start.countDown();
            boolean allThreadsDone = done.await(30, TimeUnit.SECONDS);

            assertThat(allThreadsReady).isTrue();
            assertThat(allThreadsDone).isTrue();
            assertThat(unexpectedFailures).isEmpty();
            assertThat(successCount.get()).isEqualTo(1);

            Seat heldSeat = seatRepository.findById(seatId).orElseThrow();
            assertThat(heldSeat.getStatus()).isEqualTo(SeatStatus.HELD);
            assertThat(heldSeat.getHeldByUserId()).isNotNull();
            assertThat(heldSeat.getHeldUntil()).isAfter(LocalDateTime.now());

            RBucket<String> holdBucket = redissonClient.getBucket("seat:" + seatId + ":held");
            assertThat(holdBucket.get()).isEqualTo(String.valueOf(heldSeat.getHeldByUserId()));
            assertThat(holdBucket.remainTimeToLive()).isPositive();
        } finally {
            executorService.shutdownNow();
        }
    }
}
