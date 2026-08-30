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
    name = "SAGA_INSTANCES",
    indexes = {
        @Index(name = "idx_saga_instances_status", columnList = "status"),
        @Index(name = "idx_saga_instances_updated_at", columnList = "updated_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaInstance {

    @Id
    @Column(name = "saga_id", length = 64)
    private String sagaId;

    @Column(name = "current_step", nullable = false, length = 100)
    private String currentStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SagaStatus status;

    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
