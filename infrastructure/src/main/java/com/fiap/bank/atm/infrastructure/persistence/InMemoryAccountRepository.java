package com.fiap.bank.atm.infrastructure.persistence;

import com.fiap.bank.atm.domain.model.Account;
import com.fiap.bank.atm.domain.model.Money;
import com.fiap.bank.atm.domain.model.Transaction;
import com.fiap.bank.atm.domain.model.TransactionType;
import com.fiap.bank.atm.domain.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryAccountRepository
        implements AccountRepository {

        private final Map<String, Account> accounts =
                new HashMap<>();

        public InMemoryAccountRepository() {
                seedData();
        }

        @Override
        public Optional<Account> findById(UUID id) {
                return accounts.values()
                        .stream()
                        .filter(account ->
                                account.getId().equals(id)
                        )
                        .findFirst();
        }

        @Override
        public Optional<Account> findByAccountNumber(
                String accountNumber
        ) {
                return Optional.ofNullable(
                        accounts.get(accountNumber)
                );
        }

        @Override
        public List<Account> findAll() {
                return new ArrayList<>(accounts.values());
        }

        @Override
        public void save(Account account) {
                accounts.put(
                        account.getAccountNumber(),
                        account
                );
        }

        @Override
        public void deleteById(UUID id) {
                accounts.entrySet()
                        .removeIf(entry ->
                                entry.getValue()
                                        .getId()
                                        .equals(id)
                        );
        }

        private void seedData() {
                Account account1 = new Account(
                        UUID.randomUUID(),
                        "12345",
                        "1234",
                        Money.of(5000.00),
                        Money.of(1500.00)
                );

                account1.seedTransaction(
                        new Transaction(
                                UUID.randomUUID(),
                                LocalDateTime.now().minusDays(3),
                                TransactionType.DEPOSIT,
                                Money.of(2000.00),
                                "Depósito em dinheiro"
                        )
                );

                account1.seedTransaction(
                        new Transaction(
                                UUID.randomUUID(),
                                LocalDateTime.now().minusDays(2),
                                TransactionType.TRANSFER_IN,
                                Money.of(500.00),
                                "Transf. de Conta 67890"
                        )
                );

                account1.seedTransaction(
                        new Transaction(
                                UUID.randomUUID(),
                                LocalDateTime.now().minusDays(1),
                                TransactionType.WITHDRAWAL,
                                Money.of(100.00),
                                "Saque eletrônico"
                        )
                );

                accounts.put(
                        account1.getAccountNumber(),
                        account1
                );

                Account account2 = new Account(
                        UUID.randomUUID(),
                        "67890",
                        "5678",
                        Money.of(1200.00),
                        Money.of(1000.00)
                );

                account2.seedTransaction(
                        new Transaction(
                                UUID.randomUUID(),
                                LocalDateTime.now().minusDays(5),
                                TransactionType.DEPOSIT,
                                Money.of(1500.00),
                                "Depósito inicial"
                        )
                );

                account2.seedTransaction(
                        new Transaction(
                                UUID.randomUUID(),
                                LocalDateTime.now().minusDays(2),
                                TransactionType.TRANSFER_OUT,
                                Money.of(500.00),
                                "Transf. para Conta 12345"
                        )
                );

                accounts.put(
                        account2.getAccountNumber(),
                        account2
                );

                Account account3 = new Account(
                        UUID.randomUUID(),
                        "99999",
                        "9999",
                        Money.of(50.00),
                        Money.of(500.00)
                );

                account3.seedTransaction(
                        new Transaction(
                                UUID.randomUUID(),
                                LocalDateTime.now().minusDays(10),
                                TransactionType.DEPOSIT,
                                Money.of(50.00),
                                "Abertura de conta"
                        )
                );

                accounts.put(
                        account3.getAccountNumber(),
                        account3
                );
        }
}