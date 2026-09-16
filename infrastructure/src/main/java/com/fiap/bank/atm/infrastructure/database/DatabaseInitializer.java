package com.fiap.bank.atm.infrastructure.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class DatabaseInitializer {

    private static final String CREATE_ACCOUNT_TABLE = """
            CREATE TABLE IF NOT EXISTS tb_account (
                id VARCHAR(36) PRIMARY KEY,
                agency VARCHAR(10) NOT NULL,
                number VARCHAR(20) NOT NULL UNIQUE,
                pin VARCHAR(10) NOT NULL,
                balance DECIMAL(15, 2) NOT NULL,
                daily_withdrawal_limit DECIMAL(15, 2) NOT NULL,
                total_withdrawn_today DECIMAL(15, 2) NOT NULL,
                status VARCHAR(20) NOT NULL,
                failed_attempts INTEGER NOT NULL
            )
            """;

    private static final String CREATE_TRANSACTION_TABLE = """
            CREATE TABLE IF NOT EXISTS tb_transaction (
                id VARCHAR(36) PRIMARY KEY,
                account_id VARCHAR(36) NOT NULL,
                type VARCHAR(20) NOT NULL,
                amount DECIMAL(15, 2) NOT NULL,
                description VARCHAR(255) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                FOREIGN KEY (account_id)
                    REFERENCES tb_account(id)
                    ON DELETE CASCADE
            )
            """;

    private static final String INSERT_ACCOUNT = """
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
            ON CONFLICT(id) DO NOTHING
            """;

    private DatabaseInitializer() {
        // Impede a instanciação da classe.
    }

    public static void initialize() {
        try (Connection connection =
                     ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                createTables(connection);
                insertInitialAccounts(connection);

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível inicializar o banco de dados.",
                    exception
            );
        }
    }

    private static void createTables(
            Connection connection
    ) throws SQLException {
        executeDDL(connection, CREATE_ACCOUNT_TABLE);
        executeDDL(connection, CREATE_TRANSACTION_TABLE);
    }

    private static void executeDDL(
            Connection connection,
            String sql
    ) throws SQLException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.execute();
        }
    }

    private static void insertInitialAccounts(
            Connection connection
    ) throws SQLException {
        insertAccount(
                connection,
                "550e8400-e29b-41d4-a716-446655440000",
                "0001",
                "12345",
                "1234",
                new BigDecimal("5000.00"),
                new BigDecimal("1500.00"),
                "ACTIVE"
        );

        insertAccount(
                connection,
                "550e8400-e29b-41d4-a716-446655440001",
                "0001",
                "67890",
                "5678",
                new BigDecimal("1200.00"),
                new BigDecimal("1000.00"),
                "ACTIVE"
        );

        insertAccount(
                connection,
                "550e8400-e29b-41d4-a716-446655440002",
                "0002",
                "99999",
                "9999",
                new BigDecimal("50.00"),
                new BigDecimal("500.00"),
                "ACTIVE"
        );
    }

    private static void insertAccount(
            Connection connection,
            String id,
            String agency,
            String number,
            String pin,
            BigDecimal balance,
            BigDecimal dailyWithdrawalLimit,
            String status
    ) throws SQLException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(INSERT_ACCOUNT)) {

            preparedStatement.setString(1, id);
            preparedStatement.setString(2, agency);
            preparedStatement.setString(3, number);
            preparedStatement.setString(4, pin);
            preparedStatement.setBigDecimal(5, balance);
            preparedStatement.setBigDecimal(
                    6,
                    dailyWithdrawalLimit
            );
            preparedStatement.setBigDecimal(
                    7,
                    BigDecimal.ZERO
            );
            preparedStatement.setString(8, status);
            preparedStatement.setInt(9, 0);

            preparedStatement.executeUpdate();
        }
    }
}