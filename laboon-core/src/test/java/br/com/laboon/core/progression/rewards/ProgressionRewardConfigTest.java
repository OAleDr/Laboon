package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.rewards.Reward;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgressionRewardConfigTest {

    @Test
    void shouldStoreLevelReward() {
        ProgressionRewardConfig config =
                new ProgressionRewardConfig();

        Reward reward =
                new Reward()
                        .addCoins(100);

        config.addLevelReward(
                new LevelReward(
                        5,
                        reward
                )
        );

        assertTrue(
                config.findLevelReward(5).isPresent()
        );

        assertSame(
                reward,
                config.findLevelReward(5)
                        .orElseThrow()
                        .reward()
        );
    }

    @Test
    void shouldStorePrestigeReward() {
        ProgressionRewardConfig config =
                new ProgressionRewardConfig();

        Reward reward =
                new Reward()
                        .addTokens(10);

        config.addPrestigeReward(
                new PrestigeReward(
                        2,
                        reward
                )
        );

        assertTrue(
                config.findPrestigeReward(2).isPresent()
        );

        assertSame(
                reward,
                config.findPrestigeReward(2)
                        .orElseThrow()
                        .reward()
        );
    }
}