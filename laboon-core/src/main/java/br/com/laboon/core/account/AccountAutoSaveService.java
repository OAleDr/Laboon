package br.com.laboon.core.account;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class AccountAutoSaveService {

    private final AccountManager accountManager;
    private final Collection<Account> accounts;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    public AccountAutoSaveService(
            AccountManager accountManager,
            Collection<Account> accounts
    ) {

        this.accountManager = accountManager;
        this.accounts = accounts;
    }

    public void start() {

        executor.scheduleAtFixedRate(
                this::saveAll,
                60,
                60,
                TimeUnit.SECONDS
        );
    }

    private void saveAll() {

        for (Account account : accounts) {

            try {

                accountManager.save(account);

            } catch (Exception exception) {

                exception.printStackTrace();
            }
        }
    }

    public void stop() {

        executor.shutdownNow();
    }
}