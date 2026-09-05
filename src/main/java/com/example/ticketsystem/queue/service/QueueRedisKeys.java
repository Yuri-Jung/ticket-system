package com.example.ticketsystem.queue.service;

public final class QueueRedisKeys {

    public static final String WAITING_KEY = "queue:waiting";
    public static final String WAITING_SEQUENCE_KEY = "queue:waiting:seq";
    public static final String ACTIVE_TOKEN_PREFIX = "queue:active:";
    public static final String ACTIVE_USER_PREFIX = "queue:active:user:";

    private QueueRedisKeys() {
    }

    public static String activeTokenKey(String token) {
        return ACTIVE_TOKEN_PREFIX + token;
    }

    public static String activeUserKey(Long userId) {
        return ACTIVE_USER_PREFIX + userId;
    }
}
