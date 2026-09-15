package br.com.laboon.core.rewards;

import java.util.UUID;

public interface RewardClaimRepository {

    boolean tryClaim(
            String rewardId,
            UUID playerUuid,
            RewardSource source
    );

    boolean hasClaimed(
            String rewardId
    );
}