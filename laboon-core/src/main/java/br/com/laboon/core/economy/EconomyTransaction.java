package br.com.laboon.core.economy;

import java.time.Instant;
import java.util.UUID;

public final class EconomyTransaction {

    private final UUID id;
    private final UUID playerUuid;
    private final EconomyCurrency currency;
    private final long amount;
    private final EconomyTransactionType type;
    private final String source;
    private final String metadata;
    private final Instant createdAt;

    public EconomyTransaction(
            UUID id,
            UUID playerUuid,
            EconomyCurrency currency,
            long amount,
            EconomyTransactionType type,
            String source,
            String metadata,
            Instant createdAt
    ) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.currency = currency;
        this.amount = amount;
        this.type = type;
        this.source = source;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public EconomyCurrency getCurrency() {
        return currency;
    }

    public long getAmount() {
        return amount;
    }

    public EconomyTransactionType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}