package br.com.laboon.core.progression.rewards;

import br.com.laboon.core.rewards.Reward;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ProgressionRewardLoader {

    private ProgressionRewardLoader() {
    }

    public static ProgressionRewardConfig load(Path file) {
        if (file == null) {
            throw new IllegalArgumentException("file não pode ser nulo.");
        }

        ProgressionRewardConfig config = new ProgressionRewardConfig();

        if (!Files.exists(file)) {
            return config;
        }

        try (InputStream input = Files.newInputStream(file)) {
            Object loaded = new Yaml().load(input);

            if (!(loaded instanceof Map<?, ?> root)) {
                return config;
            }

            loadLevels(root.get("levels"), config);
            loadPrestiges(root.get("prestiges"), config);

            return config;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar rewards de progression: " + file,
                    exception
            );
        }
    }

    private static void loadLevels(
            Object section,
            ProgressionRewardConfig config
    ) {
        if (!(section instanceof Map<?, ?> levels)) {
            return;
        }

        for (Map.Entry<?, ?> entry : levels.entrySet()) {
            int level = parseInt(entry.getKey());

            if (!(entry.getValue() instanceof Map<?, ?> values)) {
                continue;
            }

            Reward reward = parseReward(values);

            if (!reward.isEmpty()) {
                config.addLevelReward(
                        new LevelReward(level, reward)
                );
            }
        }
    }

    private static void loadPrestiges(
            Object section,
            ProgressionRewardConfig config
    ) {
        if (!(section instanceof Map<?, ?> prestiges)) {
            return;
        }

        for (Map.Entry<?, ?> entry : prestiges.entrySet()) {
            int prestige = parseInt(entry.getKey());

            if (!(entry.getValue() instanceof Map<?, ?> values)) {
                continue;
            }

            Reward reward = parseReward(values);

            if (!reward.isEmpty()) {
                config.addPrestigeReward(
                        new PrestigeReward(prestige, reward)
                );
            }
        }
    }

    private static Reward parseReward(Map<?, ?> values) {
        Reward reward = new Reward();

        Object coins = values.get("coins");
        if (coins != null) {
            reward.addCoins(parseLong(coins));
        }

        Object tokens = values.get("tokens");
        if (tokens != null) {
            reward.addTokens(parseLong(tokens));
        }

        Object experience = values.get("experience");
        if (experience != null) {
            reward.addExperience(parseLong(experience));
        }

        return reward;
    }

    private static int parseInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private static long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
