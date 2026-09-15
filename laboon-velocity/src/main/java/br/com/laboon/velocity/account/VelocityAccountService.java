package br.com.laboon.velocity.account;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.AccountSessionManager;
import br.com.laboon.velocity.auth.AuthenticationResult;

import com.velocitypowered.api.proxy.Player;

public final class VelocityAccountService {

    private final AccountManager accountManager;
    private final AccountSessionManager sessionManager;

    public VelocityAccountService(
            AccountManager accountManager,
            AccountSessionManager sessionManager
    ) {

        this.accountManager = accountManager;
        this.sessionManager = sessionManager;
    }

    public Account load(Player player) {

        if (player == null) {
            return null;
        }

        return accountManager.get(
                player.getUniqueId()
        );
    }

    public Account loadOrCreate(
            Player player,
            AuthenticationResult authentication
    ) {

        if (player == null) {
            throw new IllegalArgumentException(
                    "Player não pode ser nulo."
            );
        }

        if (authentication == null) {
            throw new IllegalArgumentException(
                    "AuthenticationResult não pode ser nulo."
            );
        }

        Account account =
                accountManager.getOrCreate(
                        authentication.getUuid(),
                        authentication.getUsername(),
                        authentication.getType()
                );

        accountManager.updateLastLogin(account);

        sessionManager.create(
                player.getUniqueId(),
                account.getUniqueId()
        );

        return account;
    }

    public Account getAccount(
            Player player
    ) {

        if (player == null) {
            return null;
        }

        var session =
                sessionManager.get(
                        player.getUniqueId()
                );

        if (session == null) {
            return null;
        }

        return accountManager.get(
                session.getAccountUuid()
        );
    }

    public boolean isLogged(
            Player player
    ) {

        if (player == null) {
            return false;
        }

        return sessionManager.isLogged(
                player.getUniqueId()
        );
    }

    public void logout(
            Player player
    ) {

        if (player == null) {
            return;
        }

        var session =
                sessionManager.get(
                        player.getUniqueId()
                );

        if (session != null) {

            Account account =
                    accountManager.get(
                            session.getAccountUuid()
                    );

            if (account != null) {

                accountManager.saveAndUnload(
                        account
                );
            }
        }

        sessionManager.remove(
                player.getUniqueId()
        );
    }
}