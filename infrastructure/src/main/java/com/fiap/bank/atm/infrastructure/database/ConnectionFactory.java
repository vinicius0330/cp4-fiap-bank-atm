package com.fiap.bank.atm.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica responsável por fornecer conexões JDBC
 * com o banco de dados SQLite.
 */
public final class ConnectionFactory {

    private static final String DATABASE_URL =
            "jdbc:sqlite:fiap-bank-atm.db";

    private ConnectionFactory() {
        // Impede a instanciação da classe.
    }

    /**
     * Abre e devolve uma conexão com o banco.
     *
     * @return conexão JDBC aberta
     */
    public static Connection getConnection() {
        try {
            Connection connection =
                    DriverManager.getConnection(DATABASE_URL);

            enableForeignKeys(connection);

            return connection;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível conectar ao banco SQLite.",
                    exception
            );
        }
    }

    /**
     * Ativa a validação das chaves estrangeiras do SQLite.
     */
    private static void enableForeignKeys(
            Connection connection
    ) throws SQLException {
        try (var preparedStatement =
                     connection.prepareStatement(
                             "PRAGMA foreign_keys = ON"
                     )) {

            preparedStatement.execute();
        }
    }

    /**
     * Encerra uma conexão que ainda estiver aberta.
     */
    public static void closeConnection(
            Connection connection
    ) {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível fechar a conexão SQLite.",
                    exception
            );
        }
    }
}