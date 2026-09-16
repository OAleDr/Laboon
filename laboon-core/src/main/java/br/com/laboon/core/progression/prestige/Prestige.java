package br.com.laboon.core.progression.prestige;

import java.util.Objects;
import java.util.UUID;

public final class Prestige {
    private final UUID playerUuid;
    private final int prestige;

    public Prestige(UUID playerUuid, int prestige) {
        if (playerUuid == null) throw new IllegalArgumentException("UUID do player não pode ser nulo.");
        if (prestige < 0) throw new IllegalArgumentException("Prestige não pode ser negativo.");
        this.playerUuid = playerUuid;
        this.prestige = prestige;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public int getPrestige() { return prestige; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prestige other)) return false;
        return prestige == other.prestige && playerUuid.equals(other.playerUuid);
    }

    @Override
    public int hashCode() { return Objects.hash(playerUuid, prestige); }

    @Override
    public String toString() {
        return "Prestige{" + "playerUuid=" + playerUuid + ", prestige=" + prestige + '}';
    }
}
