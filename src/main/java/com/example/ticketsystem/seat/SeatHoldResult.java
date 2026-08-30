package com.example.ticketsystem.seat;

import java.time.LocalDateTime;

public record SeatHoldResult(
    Long seatId,
    Long userId,
    LocalDateTime heldUntil
) {
}
