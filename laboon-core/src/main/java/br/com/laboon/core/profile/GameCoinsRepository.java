package br.com.laboon.core.profile;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

import java.util.UUID;

public final class GameCoinsRepository {

    private final JedisPooled redis;

    public GameCoinsRepository(RedisManager redisManager) {
        if (redisManager == null) {
            throw new IllegalArgumentException("RedisManager não pode ser nulo.");
        }

        this.redis = redisManager.getJedis();
    }

    public long find(UUID uniqueId, String game) {
        validateUniqueId(uniqueId);
        String key = createKey(uniqueId, game);

        String value = redis.get(key);

        if (value == null) {
            return 0L;
        }

        try {
            return Math.max(0L, Long.parseLong(value));
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    public void save(UUID uniqueId, String game, long coins) {
        validateUniqueId(uniqueId);

        String key = createKey(uniqueId, game);
        long value = Math.max(0L, coins);

        redis.set(key, String.valueOf(value));
    }

    public boolean exists(UUID uniqueId, String game) {
        validateUniqueId(uniqueId);

        return redis.exists(createKey(uniqueId, game));
    }

    public void delete(UUID uniqueId, String game) {
        validateUniqueId(uniqueId);

        redis.del(createKey(uniqueId, game));
    }

    private String createKey(UUID uniqueId, String game) {
        return "laboon:coins:" + normalizeGame(game) + ":" + uniqueId;
    }

    private String normalizeGame(String game) {
        if (game == null || game.isBlank()) {
            throw new IllegalArgumentException("O jogo não pode ser vazio.");
        }

        return game.trim().toLowerCase();
    }

    private void validateUniqueId(UUID uniqueId) {
        if (uniqueId == null) {
            throw new IllegalArgumentException("UUID não pode ser nulo.");
        }
    }
}