package br.com.laboon.velocity.auth;

import br.com.laboon.core.account.AccountType;

import java.util.UUID;

public final class AuthenticationResult {

    private final String username;
    private final UUID uuid;
    private final AccountType type;

    public AuthenticationResult(
            String username,
            UUID uuid,
            AccountType type
    ) {
        this.username = username;
        this.uuid = uuid;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public AccountType getType() {
        return type;
    }

    public boolean isOriginal() {
        return type == AccountType.ORIGINAL;
    }

    public boolean isLaboon() {
        return type == AccountType.LABOON;
    }
}