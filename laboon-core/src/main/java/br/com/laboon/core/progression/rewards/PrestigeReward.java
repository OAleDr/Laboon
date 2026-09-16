package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.rewards.Reward;

public record PrestigeReward(
        int prestige,
        Reward reward
) {
    public PrestigeReward {
        if (prestige < 1) {
            throw new IllegalArgumentException("prestige deve ser maior que zero.");
        }
        if (reward == null || reward.isEmpty()) {
            throw new IllegalArgumentException("reward não pode ser vazio.");
        }
    }
}
