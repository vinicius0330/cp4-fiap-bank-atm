package com.fiap.bank.atm.application.exception;

public class DailyLimitExceededApplicationException extends RuntimeException {
    public DailyLimitExceededApplicationException(String message) {
        super(message);
    }
}