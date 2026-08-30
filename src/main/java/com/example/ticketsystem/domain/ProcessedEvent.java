package com.example.ticketsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "PROCESSED_EVENTS",
    indexes = {
        @Index(name = "idx_processed_events_handler_name", columnList = "handler_name"),
        @Index(name = "idx_processed_events_processed_at", columnList = "processed_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "handler_name", nullable = false, length = 100)
    private String handlerName;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
