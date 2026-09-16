package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.progression.ProgressionLevelUpListener;
import br.com.laboon.core.progression.ProgressionSnapshot;

public final class ProgressionLevelRewardListener
        implements ProgressionLevelUpListener {

    private final ProgressionRewardService rewardService;

    public ProgressionLevelRewardListener(
            ProgressionRewardService rewardService
    ) {
        if (rewardService == null) {
            throw new IllegalArgumentException(
                    "ProgressionRewardService não pode ser nulo."
            );
        }

        this.rewardService = rewardService;
    }

    @Override
    public void onLevelUp(
            ProgressionSnapshot snapshot,
            int level
    ) {
        if (snapshot == null || snapshot.getPlayerUuid() == null) {
            return;
        }

        rewardService.giveLevelReward(
                snapshot.getPlayerUuid(),
                level
        );
    }
}
