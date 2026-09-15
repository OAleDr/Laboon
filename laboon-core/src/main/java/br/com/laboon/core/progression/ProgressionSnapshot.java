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
    private final long experienceRequiredForNextLevel;

    public ProgressionSnapshot(
            UUID playerUuid,
            ProgressionGame game,
            long experience,
            int level,
            int previousLevel,
            boolean levelUp,
            long experienceIntoLevel,
            long experienceRequiredForNextLevel
    ) {
        this.playerUuid = playerUuid;
        this.game = game;
        this.experience = experience;
        this.level = level;
        this.previousLevel = previousLevel;
        this.levelUp = levelUp;
        this.experienceIntoLevel = experienceIntoLevel;
        this.experienceRequiredForNextLevel = experienceRequiredForNextLevel;
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

    public long getExperienceRequiredForNextLevel() {
        return experienceRequiredForNextLevel;
    }

    public double getProgressPercent() {
        if (experienceRequiredForNextLevel == Long.MAX_VALUE) {
            return 100.0D;
        }
        long totalSpan = experienceRequiredForNextLevel - experienceRequiredForCurrentLevel();
        if (totalSpan <= 0L) {
            return 100.0D;
        }
        double percent = (double) experienceIntoLevel / (double) totalSpan * 100.0D;
        return Math.max(0.0D, Math.min(100.0D, percent));
    }

    private long experienceRequiredForCurrentLevel() {
        if (level <= 1) {
            return 0L;
        }
        return 0L;
    }
}
