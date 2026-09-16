package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.rewards.RewardResult;
import br.com.laboon.core.rewards.RewardService;
import br.com.laboon.core.rewards.RewardSource;

import java.util.UUID;

public final class ProgressionRewardService {

    private final RewardService rewardService;
    private final LevelRewardRepository levelRewards;
    private final PrestigeRewardRepository prestigeRewards;

    public ProgressionRewardService(
            RewardService rewardService,
            LevelRewardRepository levelRewards,
            PrestigeRewardRepository prestigeRewards
    ) {
        if (rewardService == null) {
            throw new IllegalArgumentException("RewardService não pode ser nulo.");
        }
        if (levelRewards == null) {
            throw new IllegalArgumentException("LevelRewardRepository não pode ser nulo.");
        }
        if (prestigeRewards == null) {
            throw new IllegalArgumentException("PrestigeRewardRepository não pode ser nulo.");
        }

        this.rewardService = rewardService;
        this.levelRewards = levelRewards;
        this.prestigeRewards = prestigeRewards;
    }

    public RewardResult giveLevelReward(UUID playerUuid, int level) {
        return levelRewards.findLevelReward(level)
                .map(reward ->
                        rewardService.give(
                                playerUuid,
                                reward.reward(),
                                RewardSource.EVENT,
                                "progression:level:" + level
                        )
                )
                .orElseGet(() ->
                        RewardResult.failure(
                                RewardResult.Status.FAILED
                        )
                );
    }

    public RewardResult givePrestigeReward(
            UUID playerUuid,
            int prestige
    ) {
        return prestigeRewards.findPrestigeReward(prestige)
                .map(reward ->
                        rewardService.give(
                                playerUuid,
                                reward.reward(),
                                RewardSource.SEASON,
                                "progression:prestige:" + prestige
                        )
                )
                .orElseGet(() ->
                        RewardResult.failure(
                                RewardResult.Status.FAILED
                        )
                );
    }

    public RewardService getRewardService() {
        return rewardService;
    }
}
