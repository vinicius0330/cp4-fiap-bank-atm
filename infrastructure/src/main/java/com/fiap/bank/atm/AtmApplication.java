package com.fiap.bank.atm;

import com.fiap.bank.atm.application.AtmService;
import com.fiap.bank.atm.domain.repository.AccountRepository;
import com.fiap.bank.atm.infrastructure.database.DatabaseInitializer;
import com.fiap.bank.atm.infrastructure.persistence.AccountRepositoryJdbcImpl;
import com.fiap.bank.atm.presentation.AtmFrame;

import javax.swing.SwingUtilities;

public class AtmApplication {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();

        AccountRepository accountRepository =
                new AccountRepositoryJdbcImpl();

        AtmService atmService =
                new AtmService(accountRepository);

        SwingUtilities.invokeLater(() -> {
            AtmFrame mainFrame =
                    new AtmFrame(atmService);

            mainFrame.setVisible(true);
        });
    }
}