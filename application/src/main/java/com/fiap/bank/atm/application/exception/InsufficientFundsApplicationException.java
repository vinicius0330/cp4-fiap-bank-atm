package com.fiap.bank.atm.application.exception;

public class InsufficientFundsApplicationException extends RuntimeException {
    public InsufficientFundsApplicationException(String message) {
        super(message);
    }
}
