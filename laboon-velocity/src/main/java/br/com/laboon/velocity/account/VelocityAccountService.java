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

        return accountManager.get(
                player.getUniqueId()
        );
    }

    public Account loadOrCreate(
            Player player,
            AuthenticationResult authentication
    ) {

        Account account = accountManager.getOrCreate(
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

        var session = sessionManager.get(
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

        return sessionManager.isLogged(
                player.getUniqueId()
        );
    }

    public void logout(
            Player player
    ) {

        sessionManager.remove(
                player.getUniqueId()
        );
    }
}