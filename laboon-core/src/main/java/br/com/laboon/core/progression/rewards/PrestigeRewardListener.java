package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.progression.prestige.PrestigeLevelUpEvent;
import br.com.laboon.core.progression.prestige.PrestigeListener;

public final class PrestigeRewardListener
        implements PrestigeListener {

    private final ProgressionRewardService rewardService;

    public PrestigeRewardListener(
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
    public void onPrestige(
            PrestigeLevelUpEvent event
    ) {
        if (event == null) {
            return;
        }

        rewardService.givePrestigeReward(
                event.getPlayerUuid(),
                event.getNewPrestige()
        );
    }
}
