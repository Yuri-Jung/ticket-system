package com.example.ticketsystem.seat.exception;

public class SeatNotFoundException extends SeatHoldException {

    public SeatNotFoundException(Long seatId) {
        super("Seat not found. seatId=" + seatId);
    }
}
