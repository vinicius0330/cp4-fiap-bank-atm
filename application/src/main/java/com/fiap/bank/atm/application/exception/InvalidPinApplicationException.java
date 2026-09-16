package com.fiap.bank.atm.application.exception;

public class InvalidPinApplicationException extends RuntimeException {
    public InvalidPinApplicationException(String message) {
        super(message);
    }
}