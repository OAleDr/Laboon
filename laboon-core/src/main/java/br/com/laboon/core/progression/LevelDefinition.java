package br.com.laboon.core.progression;

public record LevelDefinition(
        int level,
        long requiredTotalExperience
) {
    public LevelDefinition {
        if (level < 1) {
            throw new IllegalArgumentException("Level deve ser maior ou igual a 1.");
        }
        if (requiredTotalExperience < 0L) {
            throw new IllegalArgumentException("XP necessária não pode ser negativa.");
        }
    }
}
