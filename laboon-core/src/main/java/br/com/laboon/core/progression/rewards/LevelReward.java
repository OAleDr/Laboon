package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.rewards.Reward;

public record LevelReward(
        int level,
        Reward reward
) {
    public LevelReward {
        if (level < 1) {
            throw new IllegalArgumentException("level deve ser maior que zero.");
        }
        if (reward == null || reward.isEmpty()) {
            throw new IllegalArgumentException("reward não pode ser vazio.");
        }
    }
}
