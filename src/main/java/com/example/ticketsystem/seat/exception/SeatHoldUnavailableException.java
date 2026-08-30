package com.example.ticketsystem.seat.exception;

public class SeatHoldUnavailableException extends SeatHoldException {

    public SeatHoldUnavailableException(Long seatId) {
        super("Seat is not available to hold. seatId=" + seatId);
    }
}
