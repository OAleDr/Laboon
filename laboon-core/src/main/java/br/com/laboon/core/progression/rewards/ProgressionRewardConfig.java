package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.rewards.Reward;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class ProgressionRewardConfig
        implements LevelRewardRepository, PrestigeRewardRepository {

    private final Map<Integer, LevelReward> levelRewards = new TreeMap<>();
    private final Map<Integer, PrestigeReward> prestigeRewards = new TreeMap<>();

    public void addLevelReward(LevelReward reward) {
        if (reward == null) {
            throw new IllegalArgumentException("reward não pode ser nulo.");
        }
        levelRewards.put(reward.level(), reward);
    }

    public void addPrestigeReward(PrestigeReward reward) {
        if (reward == null) {
            throw new IllegalArgumentException("reward não pode ser nulo.");
        }
        prestigeRewards.put(reward.prestige(), reward);
    }

    public void addLevelCoins(int level, long coins) {
        addLevelReward(new LevelReward(level, new Reward().addCoins(coins)));
    }

    public void addLevelTokens(int level, long tokens) {
        addLevelReward(new LevelReward(level, new Reward().addTokens(tokens)));
    }

    public void addPrestigeCoins(int prestige, long coins) {
        addPrestigeReward(new PrestigeReward(prestige, new Reward().addCoins(coins)));
    }

    public void addPrestigeTokens(int prestige, long tokens) {
        addPrestigeReward(new PrestigeReward(prestige, new Reward().addTokens(tokens)));
    }

    @Override
    public Optional<LevelReward> findLevelReward(int level) {
        return Optional.ofNullable(levelRewards.get(level));
    }

    @Override
    public Optional<PrestigeReward> findPrestigeReward(int prestige) {
        return Optional.ofNullable(prestigeRewards.get(prestige));
    }
}
