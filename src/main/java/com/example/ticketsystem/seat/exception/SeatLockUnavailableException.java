package com.example.ticketsystem.seat.exception;

public class SeatLockUnavailableException extends SeatHoldException {

    public SeatLockUnavailableException(Long seatId) {
        super("Failed to acquire seat lock. seatId=" + seatId);
    }

    public SeatLockUnavailableException(Long seatId, Throwable cause) {
        super("Interrupted while acquiring seat lock. seatId=" + seatId, cause);
    }
}
