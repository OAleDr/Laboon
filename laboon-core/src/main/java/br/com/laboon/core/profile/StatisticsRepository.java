package br.com.laboon.core.profile;

import br.com.laboon.core.redis.RedisManager;

import redis.clients.jedis.JedisPooled;

import java.util.Map;
import java.util.UUID;

public final class StatisticsRepository {

    private final JedisPooled redis;

    public StatisticsRepository(RedisManager redisManager) {

        this.redis = redisManager.getJedis();
    }

    public void save(UUID uniqueId, Statistics statistics) {

        String key = "laboon:statistics:" + statistics.getGame() + ":" + uniqueId;

        Map<String, Long> values = statistics.getAll();

        if (values.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Long> entry : values.entrySet()) {

            redis.hset(key, entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    public Statistics find(UUID uniqueId, String game) {

        String normalizedGame = game.trim().toLowerCase();

        String key = "laboon:statistics:" + normalizedGame + ":" + uniqueId;

        Map<String, String> data = redis.hgetAll(key);

        Statistics statistics = new Statistics(normalizedGame);

        for (Map.Entry<String, String> entry : data.entrySet()) {

            try {

                statistics.set(entry.getKey(), Long.parseLong(entry.getValue()));

            } catch (NumberFormatException ignored) {
                // Ignora valores inválidos.
            }
        }

        return statistics;
    }

    public boolean exists(UUID uniqueId, String game) {

        return redis.exists("laboon:statistics:" + game.trim().toLowerCase() + ":" + uniqueId);
    }

    public void delete(UUID uniqueId, String game) {

        redis.del("laboon:statistics:" + game.trim().toLowerCase() + ":" + uniqueId);
    }
}