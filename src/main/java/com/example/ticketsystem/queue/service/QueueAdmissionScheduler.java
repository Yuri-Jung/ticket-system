package com.example.ticketsystem.queue.service;

import com.example.ticketsystem.queue.config.QueueProperties;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueAdmissionScheduler {

    private static final String POP_WAITING_USERS_SCRIPT = """
        local limit = tonumber(ARGV[1])
        local users = redis.call('ZRANGE', KEYS[1], 0, limit - 1)
        if #users > 0 then
            redis.call('ZREM', KEYS[1], unpack(users))
        end
        return users
        """;

    private final RedissonClient redissonClient;
    private final QueueProperties queueProperties;

    @Scheduled(fixedRate = 1000)
    public void admitWaitingUsersOnSchedule() {
        if (!queueProperties.isSchedulerEnabled()) {
            return;
        }

        admitWaitingUsers();
    }

    public List<QueueAdmission> admitWaitingUsers() {
        int limit = Math.max(1, queueProperties.getAdmissionBatchSize());
        List<String> userIds = redissonClient.getScript(StringCodec.INSTANCE)
            .eval(
                RScript.Mode.READ_WRITE,
                POP_WAITING_USERS_SCRIPT,
                RScript.ReturnType.MULTI,
                List.of(QueueRedisKeys.WAITING_KEY),
                Integer.toString(limit)
            );

        return userIds.stream()
            .map(this::issueActiveToken)
            .toList();
    }

    private QueueAdmission issueActiveToken(String userId) {
        String token = UUID.randomUUID().toString();

        RBucket<String> tokenBucket = redissonClient.getBucket(
            QueueRedisKeys.activeTokenKey(token),
            StringCodec.INSTANCE
        );
        tokenBucket.set(userId, queueProperties.getActiveTokenTtl());

        RBucket<String> userBucket = redissonClient.getBucket(
            QueueRedisKeys.activeUserKey(Long.valueOf(userId)),
            StringCodec.INSTANCE
        );
        userBucket.set(token, queueProperties.getActiveTokenTtl());

        return new QueueAdmission(Long.valueOf(userId), token);
    }
}
