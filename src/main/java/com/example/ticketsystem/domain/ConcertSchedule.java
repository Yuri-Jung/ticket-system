package com.example.ticketsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "CONCERT_SCHEDULES",
    indexes = {
        @Index(name = "idx_concert_schedules_concert_id", columnList = "concert_id"),
        @Index(name = "idx_concert_schedules_concert_at", columnList = "concert_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Column(name = "concert_at", nullable = false)
    private LocalDateTime concertAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ConcertSchedule(Concert concert, LocalDateTime concertAt, LocalDateTime createdAt) {
        this.concert = concert;
        this.concertAt = concertAt;
        this.createdAt = createdAt;
    }
}
