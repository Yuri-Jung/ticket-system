package com.example.ticketsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "SEATS",
    indexes = {
        @Index(name = "idx_seats_schedule_id", columnList = "concert_schedule_id"),
        @Index(name = "idx_seats_status", columnList = "status"),
        @Index(name = "idx_seats_held_until", columnList = "held_until"),
        @Index(name = "idx_seats_held_by_user_id", columnList = "held_by_user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_seats_schedule_seat_number",
            columnNames = {"concert_schedule_id", "seat_number"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concert_schedule_id", nullable = false)
    private ConcertSchedule concertSchedule;

    @Column(name = "seat_number", nullable = false, length = 50)
    private String seatNumber;

    @Column(nullable = false, length = 50)
    private String grade;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status;

    @Column(name = "held_by_user_id")
    private Long heldByUserId;

    @Column(name = "held_until")
    private LocalDateTime heldUntil;

    public Seat(
        ConcertSchedule concertSchedule,
        String seatNumber,
        String grade,
        BigDecimal price,
        SeatStatus status
    ) {
        this.concertSchedule = concertSchedule;
        this.seatNumber = seatNumber;
        this.grade = grade;
        this.price = price;
        this.status = status;
    }

    public boolean isHoldableAt(LocalDateTime now) {
        return status == SeatStatus.AVAILABLE
            || (status == SeatStatus.HELD && heldUntil != null && heldUntil.isBefore(now));
    }

    public void hold(Long userId, LocalDateTime heldUntil) {
        this.status = SeatStatus.HELD;
        this.heldByUserId = userId;
        this.heldUntil = heldUntil;
    }
}
