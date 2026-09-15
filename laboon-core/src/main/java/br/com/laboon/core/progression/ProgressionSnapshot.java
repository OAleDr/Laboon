package br.com.laboon.core.progression;

import java.util.UUID;

public final class ProgressionSnapshot {

    private final UUID playerUuid;
    private final ProgressionGame game;
    private final long experience;
    private final int level;
    private final int previousLevel;
    private final boolean levelUp;
    private final long experienceIntoLevel;
    private final long experienceForNextLevel;

    public ProgressionSnapshot(
            UUID playerUuid,
            ProgressionGame game,
            long experience,
            int level,
            int previousLevel,
            boolean levelUp,
            long experienceIntoLevel,
            long experienceForNextLevel
    ) {
        this.playerUuid = playerUuid;
        this.game = game;
        this.experience = experience;
        this.level = level;
        this.previousLevel = previousLevel;
        this.levelUp = levelUp;
        this.experienceIntoLevel = experienceIntoLevel;
        this.experienceForNextLevel = experienceForNextLevel;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public ProgressionGame getGame() {
        return game;
    }

    public long getExperience() {
        return experience;
    }

    public int getLevel() {
        return level;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public boolean isLevelUp() {
        return levelUp;
    }

    public long getExperienceIntoLevel() {
        return experienceIntoLevel;
    }

    public long getExperienceForNextLevel() {
        return experienceForNextLevel;
    }
}
