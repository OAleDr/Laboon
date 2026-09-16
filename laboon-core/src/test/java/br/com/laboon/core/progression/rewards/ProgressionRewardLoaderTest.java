package br.com.laboon.core.progression.rewards;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProgressionRewardLoaderTest {

    @Test
    void shouldLoadYamlRewards() throws Exception {
        Path file = Files.createTempFile(
                "laboon-progression-rewards",
                ".yml"
        );

        try {
            Files.writeString(
                    file,
                    """
                    levels:
                      5:
                        coins: 100
                      10:
                        coins: 250
                        tokens: 10

                    prestiges:
                      1:
                        coins: 1000
                      2:
                        coins: 2000
                        tokens: 50
                    """
            );

            ProgressionRewardConfig config =
                    ProgressionRewardLoader.load(file);

            assertTrue(config.findLevelReward(5).isPresent());
            assertTrue(config.findLevelReward(10).isPresent());
            assertTrue(config.findPrestigeReward(1).isPresent());
            assertTrue(config.findPrestigeReward(2).isPresent());
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
