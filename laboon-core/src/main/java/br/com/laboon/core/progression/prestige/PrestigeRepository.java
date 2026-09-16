package br.com.laboon.core.progression.prestige;

import java.util.UUID;

public interface PrestigeRepository {
    int getPrestige(UUID playerUuid);
    void setPrestige(UUID playerUuid, int prestige);
}
