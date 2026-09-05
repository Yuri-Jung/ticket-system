package com.example.ticketsystem.queue;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticketsystem.queue.interceptor.QueueTokenInterceptor;
import com.example.ticketsystem.queue.service.QueueAdmission;
import com.example.ticketsystem.queue.service.QueueAdmissionScheduler;
import com.example.ticketsystem.queue.service.QueueService;
import com.example.ticketsystem.queue.service.QueueStatus;
import com.example.ticketsystem.queue.service.QueueStatusResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "ticket.queue.scheduler-enabled=false")
@ActiveProfiles("test")
class QueueAdmissionIntegrationTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueAdmissionScheduler queueAdmissionScheduler;

    @Autowired
    private QueueTokenInterceptor queueTokenInterceptor;

    @Autowired
    private RedissonClient redissonClient;

    @BeforeEach
    void setUp() {
        redissonClient.getKeys().deleteByPattern("queue:*");
    }

    @Test
    void registerPromoteIssueTokenAndPassInterceptor() throws Exception {
        Long userId = 1L;

        QueueStatusResponse waiting = queueService.registerWaiting(userId);

        assertThat(waiting.status()).isEqualTo(QueueStatus.WAITING);
        assertThat(waiting.rank()).isZero();
        assertThat(waiting.estimatedWaitTimeMs()).isEqualTo(1000);
        assertThat(waiting.nextPollingIntervalMs()).isEqualTo(1000);

        List<QueueAdmission> admissions = queueAdmissionScheduler.admitWaitingUsers();

        assertThat(admissions).hasSize(1);
        assertThat(admissions.get(0).userId()).isEqualTo(userId);
        assertThat(admissions.get(0).token()).isNotBlank();

        QueueStatusResponse active = queueService.getStatus(userId);

        assertThat(active.status()).isEqualTo(QueueStatus.ACTIVE);
        assertThat(active.token()).isEqualTo(admissions.get(0).token());

        MockHttpServletRequest validRequest = new MockHttpServletRequest(
            "POST",
            "/api/v1/reservations/seats"
        );
        validRequest.addHeader(QueueTokenInterceptor.QUEUE_TOKEN_HEADER, active.token());
        MockHttpServletResponse validResponse = new MockHttpServletResponse();

        assertThat(queueTokenInterceptor.preHandle(validRequest, validResponse, new Object()))
            .isTrue();
        assertThat(validRequest.getAttribute(QueueTokenInterceptor.QUEUE_USER_ID_ATTRIBUTE))
            .isEqualTo(userId.toString());

        MockHttpServletRequest invalidRequest = new MockHttpServletRequest(
            "POST",
            "/api/v1/reservations/seats"
        );
        MockHttpServletResponse invalidResponse = new MockHttpServletResponse();

        assertThat(queueTokenInterceptor.preHandle(invalidRequest, invalidResponse, new Object()))
            .isFalse();
        assertThat(invalidResponse.getStatus()).isEqualTo(403);
    }

    @Test
    void calculateAdaptivePollingIntervalByRank() {
        assertThat(queueService.calculateNextPollingIntervalMs(5001)).isEqualTo(10000);
        assertThat(queueService.calculateNextPollingIntervalMs(5000)).isEqualTo(5000);
        assertThat(queueService.calculateNextPollingIntervalMs(1001)).isEqualTo(5000);
        assertThat(queueService.calculateNextPollingIntervalMs(1000)).isEqualTo(3000);
        assertThat(queueService.calculateNextPollingIntervalMs(201)).isEqualTo(3000);
        assertThat(queueService.calculateNextPollingIntervalMs(200)).isEqualTo(1000);
    }
}
