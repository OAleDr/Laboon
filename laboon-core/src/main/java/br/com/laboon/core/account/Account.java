package br.com.laboon.core.account;

import java.time.Instant;
import java.util.UUID;

public class Account {

    private final UUID uniqueId;

    private String name;

    private String rank;

    private long coins;

    private long experience;

    private AccountType type;

    private final Instant createdAt;

    private Instant lastLogin;

    private AccountPreferences preferences;

    public Account(
            UUID uniqueId,
            String name
    ) {

        this(
                uniqueId,
                name,
                AccountType.ORIGINAL,
                Instant.now(),
                null,
                new AccountPreferences()
        );
    }

    public Account(
            UUID uniqueId,
            String name,
            AccountType type,
            Instant createdAt,
            Instant lastLogin,
            AccountPreferences preferences
    ) {

        this.uniqueId = uniqueId;

        this.name = name;

        this.rank = "DEFAULT";

        this.coins = 0;

        this.experience = 0;

        this.type = type;

        this.createdAt = createdAt;

        this.lastLogin = lastLogin;

        this.preferences = preferences;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name
    ) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(
            String rank
    ) {
        this.rank = rank;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(
            long coins
    ) {
        this.coins = coins;
    }

    public long getExperience() {
        return experience;
    }

    public void setExperience(
            long experience
    ) {
        this.experience = experience;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(
            AccountType type
    ) {
        this.type = type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(
            Instant lastLogin
    ) {
        this.lastLogin = lastLogin;
    }

    public AccountPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(
            AccountPreferences preferences
    ) {
        this.preferences = preferences;
    }
}