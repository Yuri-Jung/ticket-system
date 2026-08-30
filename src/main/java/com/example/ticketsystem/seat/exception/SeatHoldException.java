package com.example.ticketsystem.seat.exception;

public class SeatHoldException extends RuntimeException {

    public SeatHoldException(String message) {
        super(message);
    }

    public SeatHoldException(String message, Throwable cause) {
        super(message, cause);
    }
}
