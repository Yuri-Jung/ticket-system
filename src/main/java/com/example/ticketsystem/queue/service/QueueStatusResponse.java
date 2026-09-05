package com.example.ticketsystem.queue.service;

public record QueueStatusResponse(
    QueueStatus status,
    Long rank,
    Long estimatedWaitTimeMs,
    Long nextPollingIntervalMs,
    String token
) {

    public static QueueStatusResponse waiting(
        long rank,
        long estimatedWaitTimeMs,
        long nextPollingIntervalMs
    ) {
        return new QueueStatusResponse(
            QueueStatus.WAITING,
            rank,
            estimatedWaitTimeMs,
            nextPollingIntervalMs,
            null
        );
    }

    public static QueueStatusResponse active(String token) {
        return new QueueStatusResponse(QueueStatus.ACTIVE, null, null, null, token);
    }

    public static QueueStatusResponse notFound() {
        return new QueueStatusResponse(QueueStatus.NOT_FOUND, null, null, null, null);
    }
}
