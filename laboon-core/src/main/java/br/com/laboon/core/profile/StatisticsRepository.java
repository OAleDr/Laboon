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

    /*
     * =========================
     * SAVE
     * =========================
     */

    public void save(UUID uniqueId, Statistics statistics) {

        String key = createKey(uniqueId, statistics.getGame(), statistics.getMode());

        Map<String, Long> values = statistics.getAll();

        if (values.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Long> entry : values.entrySet()) {

            redis.hset(key, entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    /*
     * =========================
     * FIND - GERAL
     * =========================
     */

    public Statistics find(UUID uniqueId, String game) {

        return find(uniqueId, game, null);
    }

    /*
     * =========================
     * FIND - MODO
     * =========================
     */

    public Statistics find(UUID uniqueId, String game, String mode) {

        String normalizedGame = normalize(game);

        String normalizedMode = normalizeNullable(mode);

        String key = createKey(uniqueId, normalizedGame, normalizedMode);

        Map<String, String> data = redis.hgetAll(key);

        Statistics statistics = new Statistics(normalizedGame, normalizedMode);

        for (Map.Entry<String, String> entry : data.entrySet()) {

            try {

                statistics.set(entry.getKey(), Long.parseLong(entry.getValue()));

            } catch (NumberFormatException ignored) {

                // Ignora valores inválidos.
            }
        }

        return statistics;
    }

    /*
     * =========================
     * EXISTS
     * =========================
     */

    public boolean exists(UUID uniqueId, String game) {

        return exists(uniqueId, game, null);
    }

    public boolean exists(UUID uniqueId, String game, String mode) {

        return redis.exists(createKey(uniqueId, normalize(game), normalizeNullable(mode)));
    }

    /*
     * =========================
     * DELETE
     * =========================
     */

    public void delete(UUID uniqueId, String game) {

        delete(uniqueId, game, null);
    }

    public void delete(UUID uniqueId, String game, String mode) {

        redis.del(createKey(uniqueId, normalize(game), normalizeNullable(mode)));
    }

    /*
     * =========================
     * REDIS KEY
     * =========================
     */

    private String createKey(UUID uniqueId, String game, String mode) {

        if (mode == null) {

            return "laboon:statistics:" + game + ":" + uniqueId;
        }

        return "laboon:statistics:" + game + ":" + mode + ":" + uniqueId;
    }

    /*
     * =========================
     * NORMALIZAÇÃO
     * =========================
     */

    private String normalize(String value) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException("O valor não pode ser vazio.");
        }

        return value.trim().toLowerCase();
    }

    private String normalizeNullable(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }
}