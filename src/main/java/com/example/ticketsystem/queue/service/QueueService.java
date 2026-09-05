package com.example.ticketsystem.queue.service;

import com.example.ticketsystem.queue.config.QueueProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueService {

    private static final long SCORE_EPOCH_MILLIS = 1_700_000_000_000L;
    private static final String REGISTER_WAITING_SCRIPT = """
        local current_rank = redis.call('ZRANK', KEYS[1], ARGV[1])
        if current_rank then
            return current_rank
        end

        local sequence = redis.call('INCR', KEYS[2])
        local score = (tonumber(ARGV[2]) * 10000) + (sequence % 10000)
        redis.call('ZADD', KEYS[1], score, ARGV[1])

        return redis.call('ZRANK', KEYS[1], ARGV[1])
        """;

    private final RedissonClient redissonClient;
    private final QueueProperties queueProperties;

    public QueueStatusResponse registerWaiting(Long userId) {
        String activeToken = getActiveToken(userId);
        if (activeToken != null) {
            return QueueStatusResponse.active(activeToken);
        }

        long scoreTime = System.currentTimeMillis() - SCORE_EPOCH_MILLIS;
        Long rank = redissonClient.getScript(StringCodec.INSTANCE)
            .eval(
                RScript.Mode.READ_WRITE,
                REGISTER_WAITING_SCRIPT,
                RScript.ReturnType.INTEGER,
                List.of(QueueRedisKeys.WAITING_KEY, QueueRedisKeys.WAITING_SEQUENCE_KEY),
                userId.toString(),
                Long.toString(scoreTime)
            );

        return waitingResponse(rank);
    }

    public QueueStatusResponse getStatus(Long userId) {
        String activeToken = getActiveToken(userId);
        if (activeToken != null) {
            return QueueStatusResponse.active(activeToken);
        }

        Integer rank = waitingQueue().rank(userId.toString());
        if (rank == null) {
            return QueueStatusResponse.notFound();
        }

        return waitingResponse(rank);
    }

    public long estimateWaitTimeMs(long rank) {
        long position = rank + 1;
        long admissionBatchSize = Math.max(1, queueProperties.getAdmissionBatchSize());
        return ((position + admissionBatchSize - 1) / admissionBatchSize) * 1000;
    }

    public long calculateNextPollingIntervalMs(long rank) {
        if (rank > 5000) {
            return 10000;
        }
        if (rank > 1000) {
            return 5000;
        }
        if (rank > 200) {
            return 3000;
        }
        return 1000;
    }

    private QueueStatusResponse waitingResponse(long rank) {
        return QueueStatusResponse.waiting(
            rank,
            estimateWaitTimeMs(rank),
            calculateNextPollingIntervalMs(rank)
        );
    }

    private String getActiveToken(Long userId) {
        RBucket<String> activeUserBucket = redissonClient.getBucket(
            QueueRedisKeys.activeUserKey(userId),
            StringCodec.INSTANCE
        );
        return activeUserBucket.get();
    }

    private RScoredSortedSet<String> waitingQueue() {
        return redissonClient.getScoredSortedSet(QueueRedisKeys.WAITING_KEY, StringCodec.INSTANCE);
    }
}
