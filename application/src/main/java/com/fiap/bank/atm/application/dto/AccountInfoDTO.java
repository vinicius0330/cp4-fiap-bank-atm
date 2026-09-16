package com.fiap.bank.atm.application.dto;

import java.math.BigDecimal;

/**
 * DTO (Java Record) que representa os dados de uma conta expostos
 * pela camada de aplicação para a camada de apresentação.
 * Nunca expõe a entidade de domínio 'Account' diretamente.
 */
public record AccountInfoDTO(
        String accountNumber,
        BigDecimal balance,
        BigDecimal dailyWithdrawalLimit,
        BigDecimal totalWithdrawnToday,
        boolean blocked
) {
}
