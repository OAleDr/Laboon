package br.com.laboon.core.progression.rewards;

import java.util.Optional;

public interface LevelRewardRepository {

    Optional<LevelReward> findLevelReward(int level);
}