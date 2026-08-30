package com.example.ticketsystem.seat;

import com.example.ticketsystem.seat.exception.SeatLockUnavailableException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatReservationFacade {

    private static final long LOCK_WAIT_SECONDS = 1;
    private static final long LOCK_LEASE_SECONDS = 2;
    private static final Duration HOLD_TTL = Duration.ofMinutes(5);

    private final RedissonClient redissonClient;
    private final SeatHoldService seatHoldService;

    public SeatHoldResult holdSeat(Long seatId, Long userId) {
        RLock lock = redissonClient.getLock("lock:seat:" + seatId);

        try {
            boolean locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new SeatLockUnavailableException(seatId);
            }

            SeatHoldResult result = seatHoldService.holdSeat(seatId, userId);
            RBucket<String> holdBucket = redissonClient.getBucket("seat:" + seatId + ":held");
            holdBucket.set(String.valueOf(userId), HOLD_TTL);
            return result;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SeatLockUnavailableException(seatId, exception);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
