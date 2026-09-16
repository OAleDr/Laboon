package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.rewards.Reward;
import br.com.laboon.core.rewards.RewardResult;
import br.com.laboon.core.rewards.RewardService;
import br.com.laboon.core.rewards.RewardSource;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProgressionRewardServiceTest {

    @Test
    void shouldGiveConfiguredLevelReward() {
        UUID player = UUID.randomUUID();

        Reward reward = new Reward().addCoins(100);

        LevelRewardRepository levels = mock(LevelRewardRepository.class);
        PrestigeRewardRepository prestiges =
                mock(PrestigeRewardRepository.class);
        RewardService rewardService = mock(RewardService.class);

        when(levels.findLevelReward(5)).thenReturn(
                Optional.of(new LevelReward(5, reward))
        );

        when(
                rewardService.give(
                        eq(player),
                        eq(reward),
                        eq(RewardSource.EVENT),
                        eq("progression:level:5")
                )
        ).thenReturn(
                RewardResult.success(java.util.List.of(reward))
        );

        ProgressionRewardService service =
                new ProgressionRewardService(
                        rewardService,
                        levels,
                        prestiges
                );

        RewardResult result =
                service.giveLevelReward(player, 5);

        assertEquals(
                RewardResult.Status.SUCCESS,
                result.getStatus()
        );

        verify(rewardService).give(
                player,
                reward,
                RewardSource.EVENT,
                "progression:level:5"
        );
    }

    @Test
    void shouldGiveConfiguredPrestigeReward() {
        UUID player = UUID.randomUUID();

        Reward reward = new Reward().addCoins(1000);

        LevelRewardRepository levels = mock(LevelRewardRepository.class);
        PrestigeRewardRepository prestiges =
                mock(PrestigeRewardRepository.class);
        RewardService rewardService = mock(RewardService.class);

        when(prestiges.findPrestigeReward(2)).thenReturn(
                Optional.of(new PrestigeReward(2, reward))
        );

        when(
                rewardService.give(
                        eq(player),
                        eq(reward),
                        eq(RewardSource.SEASON),
                        eq("progression:prestige:2")
                )
        ).thenReturn(
                RewardResult.success(java.util.List.of(reward))
        );

        ProgressionRewardService service =
                new ProgressionRewardService(
                        rewardService,
                        levels,
                        prestiges
                );

        RewardResult result =
                service.givePrestigeReward(player, 2);

        assertEquals(
                RewardResult.Status.SUCCESS,
                result.getStatus()
        );

        verify(rewardService).give(
                player,
                reward,
                RewardSource.SEASON,
                "progression:prestige:2"
        );
    }
}
