package com.example.ticketsystem.queue.interceptor;

import com.example.ticketsystem.queue.service.QueueRedisKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class QueueTokenInterceptor implements HandlerInterceptor {

    public static final String QUEUE_TOKEN_HEADER = "X-Queue-Token";
    public static final String QUEUE_USER_ID_ATTRIBUTE = "queueUserId";

    private final RedissonClient redissonClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(QUEUE_TOKEN_HEADER);
        if (!StringUtils.hasText(token)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Queue token is required.");
            return false;
        }

        RBucket<String> tokenBucket = redissonClient.getBucket(
            QueueRedisKeys.activeTokenKey(token),
            StringCodec.INSTANCE
        );
        String userId = tokenBucket.get();
        if (!StringUtils.hasText(userId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Queue token is invalid or expired.");
            return false;
        }

        request.setAttribute(QUEUE_USER_ID_ATTRIBUTE, userId);
        return true;
    }
}
