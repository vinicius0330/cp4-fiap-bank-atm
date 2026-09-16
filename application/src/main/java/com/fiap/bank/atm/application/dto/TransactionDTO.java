package com.fiap.bank.atm.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Java Record) que representa uma transação exposta
 * pela camada de aplicação. O campo 'type' já vem como texto legível
 * (ex: "Saque", "Depósito"), pois a apresentação não pode depender
 * do enum de domínio TransactionType.
 */
public record TransactionDTO(
        UUID id,
        LocalDateTime timestamp,
        String type,
        BigDecimal amount,
        String description
) {
}
