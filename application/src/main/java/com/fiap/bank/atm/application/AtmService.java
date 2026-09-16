package com.fiap.bank.atm.application;

import com.fiap.bank.atm.application.dto.AccountInfoDTO;
import com.fiap.bank.atm.application.dto.TransactionDTO;
import com.fiap.bank.atm.application.exception.AccountBlockedApplicationException;
import com.fiap.bank.atm.application.exception.DailyLimitExceededApplicationException;
import com.fiap.bank.atm.application.exception.InsufficientFundsApplicationException;
import com.fiap.bank.atm.application.exception.InvalidPinApplicationException;
import com.fiap.bank.atm.domain.exception.AccountBlockedException;
import com.fiap.bank.atm.domain.exception.DailyLimitExceededException;
import com.fiap.bank.atm.domain.exception.InsufficientFundsException;
import com.fiap.bank.atm.domain.exception.InvalidPinException;
import com.fiap.bank.atm.domain.model.Account;
import com.fiap.bank.atm.domain.model.Money;
import com.fiap.bank.atm.domain.model.Transaction;
import com.fiap.bank.atm.domain.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;

public class AtmService {

    private final AccountRepository accountRepository;
    private Account currentAccount;

    public AtmService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountInfoDTO authenticate(String accountNumber, String pin) {
        Account account =
                accountRepository.findByAccountNumber(accountNumber);

        if (account == null) {
            throw new InvalidPinApplicationException(
                    "Conta não encontrada."
            );
        }

        try {
            account.authenticate(pin);
            currentAccount = account;
            accountRepository.save(account);

            return toAccountInfoDTO(account);
        } catch (AccountBlockedException exception) {
            accountRepository.save(account);

            throw new AccountBlockedApplicationException(
                    exception.getMessage()
            );
        } catch (InvalidPinException exception) {
            accountRepository.save(account);

            throw new InvalidPinApplicationException(
                    exception.getMessage()
            );
        }
    }

    public void withdraw(BigDecimal amount) {
        ensureAuthenticated();

        try {
            currentAccount.withdraw(Money.of(amount));
            accountRepository.save(currentAccount);
        } catch (AccountBlockedException exception) {
            throw new AccountBlockedApplicationException(
                    exception.getMessage()
            );
        } catch (InsufficientFundsException exception) {
            throw new InsufficientFundsApplicationException(
                    exception.getMessage()
            );
        } catch (DailyLimitExceededException exception) {
            throw new DailyLimitExceededApplicationException(
                    exception.getMessage()
            );
        }
    }

    public void deposit(BigDecimal amount) {
        ensureAuthenticated();

        try {
            currentAccount.deposit(Money.of(amount));
            accountRepository.save(currentAccount);
        } catch (AccountBlockedException exception) {
            throw new AccountBlockedApplicationException(
                    exception.getMessage()
            );
        }
    }

    public void transfer(
            String targetAccountNumber,
            BigDecimal amount
    ) {
        ensureAuthenticated();

        Account targetAccount =
                accountRepository.findByAccountNumber(
                        targetAccountNumber
                );

        if (targetAccount == null) {
            throw new IllegalArgumentException(
                    "Conta de destino não encontrada."
            );
        }

        try {
            currentAccount.transfer(
                    targetAccount,
                    Money.of(amount)
            );

            accountRepository.save(currentAccount);
            accountRepository.save(targetAccount);
        } catch (AccountBlockedException exception) {
            throw new AccountBlockedApplicationException(
                    exception.getMessage()
            );
        } catch (InsufficientFundsException exception) {
            throw new InsufficientFundsApplicationException(
                    exception.getMessage()
            );
        }
    }

    public BigDecimal getBalance() {
        ensureAuthenticated();

        return currentAccount
                .getBalance()
                .getAmount();
    }

    public AccountInfoDTO getCurrentAccountInfo() {
        ensureAuthenticated();

        return toAccountInfoDTO(currentAccount);
    }

    public List<TransactionDTO> getStatement() {
        ensureAuthenticated();

        return currentAccount
                .getTransactions()
                .stream()
                .map(this::toTransactionDTO)
                .toList();
    }

    public void logout() {
        currentAccount = null;
    }

    public boolean isAuthenticated() {
        return currentAccount != null;
    }

    private void ensureAuthenticated() {
        if (!isAuthenticated()) {
            throw new IllegalStateException(
                    "Nenhum usuário está autenticado no momento."
            );
        }
    }

    private AccountInfoDTO toAccountInfoDTO(Account account) {
        return new AccountInfoDTO(
                account.getAccountNumber(),
                account.getBalance().getAmount(),
                account.getDailyWithdrawalLimit().getAmount(),
                account.getTotalWithdrawnToday().getAmount(),
                account.isBlocked()
        );
    }

    private TransactionDTO toTransactionDTO(
            Transaction transaction
    ) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getTimestamp(),
                transaction.getType().getDescription(),
                transaction.getAmount().getAmount(),
                transaction.getDescription()
        );
    }
}