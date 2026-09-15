package br.com.laboon.core.progression;

import java.util.List;
import java.util.UUID;

public interface ProgressionRepository {

    long getExperience(
            UUID playerUuid,
            ProgressionGame game
    );

    void setExperience(
            UUID playerUuid,
            ProgressionGame game,
            long experience
    );

    void addExperience(
            UUID playerUuid,
            ProgressionGame game,
            long amount
    );

    default void saveHistory(
            ProgressionXpEntry entry
    ) {
    }

    default List<ProgressionXpEntry> getHistory(
            UUID playerUuid,
            ProgressionGame game,
            int limit
    ) {
        return List.of();
    }
}
