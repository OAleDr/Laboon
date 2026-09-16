package br.com.laboon.core.progression.prestige;

import java.sql.Connection;
import java.util.UUID;

public interface PrestigeTransactionRepository {
    int getPrestige(Connection connection, UUID playerUuid);
    void setPrestige(Connection connection, UUID playerUuid, int prestige);
}
