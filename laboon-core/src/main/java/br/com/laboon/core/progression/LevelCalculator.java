package br.com.laboon.core.progression;

public final class LevelCalculator {

    private final long baseExperience;
    private final long linearIncrease;
    private final long quadraticIncrease;
    private final int maxLevel;

    public LevelCalculator(
            long baseExperience,
            long linearIncrease,
            long quadraticIncrease,
            int maxLevel
    ) {
        if (baseExperience <= 0L) {
            throw new IllegalArgumentException("baseExperience deve ser maior que zero.");
        }
        if (linearIncrease < 0L || quadraticIncrease < 0L) {
            throw new IllegalArgumentException("Incrementos não podem ser negativos.");
        }
        if (maxLevel < 1) {
            throw new IllegalArgumentException("maxLevel deve ser maior ou igual a 1.");
        }
        this.baseExperience = baseExperience;
        this.linearIncrease = linearIncrease;
        this.quadraticIncrease = quadraticIncrease;
        this.maxLevel = maxLevel;
    }

    public static LevelCalculator defaultCalculator() {
        return new LevelCalculator(1000L, 250L, 25L, 1000);
    }

    public int levelFromExperience(long experience) {
        if (experience <= 0L) {
            return 1;
        }
        int level = 1;
        for (int next = 2; next <= maxLevel; next++) {
            if (experience < requiredExperience(next)) {
                break;
            }
            level = next;
        }
        return level;
    }

    public long requiredExperience(int level) {
        if (level <= 1) {
            return 0L;
        }
        if (level > maxLevel) {
            return Long.MAX_VALUE;
        }
        long n = level - 1L;
        try {
            long linear = Math.multiplyExact(linearIncrease, n);
            long quadratic = Math.multiplyExact(quadraticIncrease, Math.multiplyExact(n, n));
            return Math.addExact(baseExperience, Math.addExact(linear, quadratic));
        } catch (ArithmeticException exception) {
            return Long.MAX_VALUE;
        }
    }

    public long requiredExperienceForNextLevel(int currentLevel) {
        if (currentLevel >= maxLevel) {
            return Long.MAX_VALUE;
        }
        return requiredExperience(currentLevel + 1);
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
