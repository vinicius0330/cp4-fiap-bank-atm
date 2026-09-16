package com.fiap.bank.atm.infrastructure.persistence;

import com.fiap.bank.atm.domain.model.Account;
import com.fiap.bank.atm.domain.model.Money;
import com.fiap.bank.atm.domain.model.Transaction;
import com.fiap.bank.atm.domain.model.TransactionType;
import com.fiap.bank.atm.domain.repository.AccountRepository;
import com.fiap.bank.atm.infrastructure.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountRepositoryJdbcImpl
        implements AccountRepository {

    private static final String FIND_BY_ID = """
            SELECT
                id,
                number,
                pin,
                balance,
                daily_withdrawal_limit,
                total_withdrawn_today,
                status,
                failed_attempts
            FROM tb_account
            WHERE id = ?
            """;

    private static final String FIND_BY_ACCOUNT_NUMBER = """
            SELECT
                id,
                number,
                pin,
                balance,
                daily_withdrawal_limit,
                total_withdrawn_today,
                status,
                failed_attempts
            FROM tb_account
            WHERE number = ?
            """;

    private static final String FIND_ALL = """
            SELECT
                id,
                number,
                pin,
                balance,
                daily_withdrawal_limit,
                total_withdrawn_today,
                status,
                failed_attempts
            FROM tb_account
            ORDER BY number
            """;

    private static final String FIND_TRANSACTIONS = """
            SELECT
                id,
                account_id,
                type,
                amount,
                description,
                created_at
            FROM tb_transaction
            WHERE account_id = ?
            ORDER BY created_at
            """;

    private static final String SAVE_ACCOUNT = """
            INSERT INTO tb_account (
                id,
                agency,
                number,
                pin,
                balance,
                daily_withdrawal_limit,
                total_withdrawn_today,
                status,
                failed_attempts
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                agency = excluded.agency,
                number = excluded.number,
                pin = excluded.pin,
                balance = excluded.balance,
                daily_withdrawal_limit =
                    excluded.daily_withdrawal_limit,
                total_withdrawn_today =
                    excluded.total_withdrawn_today,
                status = excluded.status,
                failed_attempts = excluded.failed_attempts
            """;

    private static final String DELETE_TRANSACTIONS = """
            DELETE FROM tb_transaction
            WHERE account_id = ?
            """;

    private static final String INSERT_TRANSACTION = """
            INSERT INTO tb_transaction (
                id,
                account_id,
                type,
                amount,
                description,
                created_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String DELETE_ACCOUNT = """
            DELETE FROM tb_account
            WHERE id = ?
            """;

    @Override
    public Optional<Account> findById(UUID id) {
        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(FIND_BY_ID)
        ) {
            preparedStatement.setString(
                    1,
                    id.toString()
            );

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapAccount(resultSet, connection)
                    );
                }

                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw persistenceException(
                    "Não foi possível buscar a conta pelo ID.",
                    exception
            );
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(
            String accountNumber
    ) {
        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                FIND_BY_ACCOUNT_NUMBER
                        )
        ) {
            preparedStatement.setString(
                    1,
                    accountNumber
            );

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapAccount(resultSet, connection)
                    );
                }

                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw persistenceException(
                    "Não foi possível buscar a conta pelo número.",
                    exception
            );
        }
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(FIND_ALL);

                ResultSet resultSet =
                        preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                accounts.add(
                        mapAccount(resultSet, connection)
                );
            }

            return accounts;
        } catch (SQLException exception) {
            throw persistenceException(
                    "Não foi possível buscar as contas.",
                    exception
            );
        }
    }

    @Override
    public void save(Account account) {
        try (Connection connection =
                     ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                saveAccount(connection, account);
                replaceTransactions(connection, account);

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw persistenceException(
                    "Não foi possível salvar a conta.",
                    exception
            );
        }
    }

    @Override
    public void deleteById(UUID id) {
        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                DELETE_ACCOUNT
                        )
        ) {
            preparedStatement.setString(
                    1,
                    id.toString()
            );

            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw persistenceException(
                    "Não foi possível excluir a conta.",
                    exception
            );
        }
    }

    private Account mapAccount(
            ResultSet resultSet,
            Connection connection
    ) throws SQLException {
        UUID accountId = UUID.fromString(
                resultSet.getString("id")
        );

        List<Transaction> transactions =
                findTransactionsByAccountId(
                        accountId,
                        connection
                );

        boolean blocked =
                "BLOCKED".equalsIgnoreCase(
                        resultSet.getString("status")
                );

        return new Account(
                accountId,
                resultSet.getString("number"),
                resultSet.getString("pin"),
                Money.of(
                        resultSet.getBigDecimal("balance")
                ),
                Money.of(
                        resultSet.getBigDecimal(
                                "daily_withdrawal_limit"
                        )
                ),
                Money.of(
                        resultSet.getBigDecimal(
                                "total_withdrawn_today"
                        )
                ),
                blocked,
                resultSet.getInt("failed_attempts"),
                transactions
        );
    }

    private List<Transaction> findTransactionsByAccountId(
            UUID accountId,
            Connection connection
    ) throws SQLException {
        List<Transaction> transactions =
                new ArrayList<>();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             FIND_TRANSACTIONS
                     )) {

            preparedStatement.setString(
                    1,
                    accountId.toString()
            );

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    transactions.add(
                            mapTransaction(resultSet)
                    );
                }
            }
        }

        return transactions;
    }

    private Transaction mapTransaction(
            ResultSet resultSet
    ) throws SQLException {
        return new Transaction(
                UUID.fromString(
                        resultSet.getString("id")
                ),
                LocalDateTime.parse(
                        resultSet.getString("created_at")
                ),
                TransactionType.valueOf(
                        resultSet.getString("type")
                ),
                Money.of(
                        resultSet.getBigDecimal("amount")
                ),
                resultSet.getString("description")
        );
    }

    private void saveAccount(
            Connection connection,
            Account account
    ) throws SQLException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             SAVE_ACCOUNT
                     )) {

            preparedStatement.setString(
                    1,
                    account.getId().toString()
            );
            preparedStatement.setString(
                    2,
                    "0001"
            );
            preparedStatement.setString(
                    3,
                    account.getAccountNumber()
            );
            preparedStatement.setString(
                    4,
                    account.getPin()
            );
            preparedStatement.setBigDecimal(
                    5,
                    account.getBalance().getAmount()
            );
            preparedStatement.setBigDecimal(
                    6,
                    account.getDailyWithdrawalLimit()
                            .getAmount()
            );
            preparedStatement.setBigDecimal(
                    7,
                    account.getTotalWithdrawnToday()
                            .getAmount()
            );
            preparedStatement.setString(
                    8,
                    account.isBlocked()
                            ? "BLOCKED"
                            : "ACTIVE"
            );
            preparedStatement.setInt(
                    9,
                    account.getFailedAttempts()
            );

            preparedStatement.executeUpdate();
        }
    }

    private void replaceTransactions(
            Connection connection,
            Account account
    ) throws SQLException {
        deleteTransactions(connection, account.getId());

        for (Transaction transaction :
                account.getTransactions()) {

            insertTransaction(
                    connection,
                    account.getId(),
                    transaction
            );
        }
    }

    private void deleteTransactions(
            Connection connection,
            UUID accountId
    ) throws SQLException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             DELETE_TRANSACTIONS
                     )) {

            preparedStatement.setString(
                    1,
                    accountId.toString()
            );

            preparedStatement.executeUpdate();
        }
    }

    private void insertTransaction(
            Connection connection,
            UUID accountId,
            Transaction transaction
    ) throws SQLException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             INSERT_TRANSACTION
                     )) {

            preparedStatement.setString(
                    1,
                    transaction.getId().toString()
            );
            preparedStatement.setString(
                    2,
                    accountId.toString()
            );
            preparedStatement.setString(
                    3,
                    transaction.getType().name()
            );
            preparedStatement.setBigDecimal(
                    4,
                    transaction.getAmount().getAmount()
            );
            preparedStatement.setString(
                    5,
                    transaction.getDescription()
            );
            preparedStatement.setString(
                    6,
                    transaction.getTimestamp().toString()
            );

            preparedStatement.executeUpdate();
        }
    }

    private IllegalStateException persistenceException(
            String message,
            SQLException cause
    ) {
        return new IllegalStateException(
                message,
                cause
        );
    }
}