package br.com.laboon.core.progression;

import java.util.UUID;

public interface ProgressionRepository {

    long getExperience(UUID playerUuid, ProgressionGame game);

    void setExperience(UUID playerUuid, ProgressionGame game, long experience);

    void addExperience(UUID playerUuid, ProgressionGame game, long amount);
}
