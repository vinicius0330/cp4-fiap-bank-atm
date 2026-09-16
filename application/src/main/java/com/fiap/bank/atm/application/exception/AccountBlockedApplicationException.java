package com.fiap.bank.atm.application.exception;

/**
 * Exception de aplicação equivalente à AccountBlockedException do domínio.
 * A camada de apresentação captura esta classe, nunca a de domínio.
 */
public class AccountBlockedApplicationException extends RuntimeException {
    public AccountBlockedApplicationException(String message) {
        super(message);
    }
}
