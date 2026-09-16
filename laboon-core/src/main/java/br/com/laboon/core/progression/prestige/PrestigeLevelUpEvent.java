package br.com.laboon.core.progression.prestige;

import java.time.Instant;
import java.util.UUID;

public final class PrestigeLevelUpEvent {
    private final UUID playerUuid;
    private final int previousPrestige;
    private final int newPrestige;
    private final Instant createdAt;

    public PrestigeLevelUpEvent(UUID playerUuid, int previousPrestige, int newPrestige) {
        if (playerUuid == null) throw new IllegalArgumentException("UUID do player não pode ser nulo.");
        if (previousPrestige < 0 || newPrestige < 0) throw new IllegalArgumentException("Prestige não pode ser negativo.");
        if (newPrestige <= previousPrestige) throw new IllegalArgumentException("Novo prestige deve ser maior que o anterior.");
        this.playerUuid = playerUuid;
        this.previousPrestige = previousPrestige;
        this.newPrestige = newPrestige;
        this.createdAt = Instant.now();
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public int getPreviousPrestige() { return previousPrestige; }
    public int getNewPrestige() { return newPrestige; }
    public Instant getCreatedAt() { return createdAt; }
}
