package com.example.ticketer.exception;

public class TroubleTicketNotFoundException extends RuntimeException {
    public TroubleTicketNotFoundException(String message) {
        super(message);
    }
}