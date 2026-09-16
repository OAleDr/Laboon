package br.com.laboon.core.progression.rewards;

import java.util.Optional;

public interface PrestigeRewardRepository {

    Optional<PrestigeReward> findPrestigeReward(int prestige);
}