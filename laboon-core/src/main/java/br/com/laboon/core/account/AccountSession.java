package br.com.laboon.core.account;

import java.time.Instant;
import java.util.UUID;

public final class AccountSession {

    private final UUID playerUuid;
    private final UUID accountUuid;
    private final Instant loginTime;

    public AccountSession(UUID playerUuid, UUID accountUuid) {

        this.playerUuid = playerUuid;
        this.accountUuid = accountUuid;
        this.loginTime = Instant.now();
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public UUID getAccountUuid() {
        return accountUuid;
    }

    public Instant getLoginTime() {
        return loginTime;
    }
}