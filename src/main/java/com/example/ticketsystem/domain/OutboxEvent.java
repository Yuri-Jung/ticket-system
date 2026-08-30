package com.example.ticketsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "OUTBOX_EVENTS",
    indexes = {
        @Index(name = "idx_outbox_events_status", columnList = "status"),
        @Index(name = "idx_outbox_events_created_at", columnList = "created_at"),
        @Index(name = "idx_outbox_events_aggregate", columnList = "aggregate_type, aggregate_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
