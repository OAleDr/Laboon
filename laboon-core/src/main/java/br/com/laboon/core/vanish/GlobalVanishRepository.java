package br.com.laboon.core.vanish;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

import java.util.Set;
import java.util.UUID;

public final class GlobalVanishRepository {

    private static final String PREFIX = "laboon:vanish:";

    private final JedisPooled redis;

    public GlobalVanishRepository(RedisManager redisManager) {
        if (redisManager == null) {
            throw new IllegalArgumentException("RedisManager não pode ser nulo.");
        }

        this.redis = redisManager.getJedis();
    }

    public boolean isVanished(UUID playerUuid) {
        if (playerUuid == null) {
            return false;
        }

        return redis.exists(key(playerUuid));
    }

    public void setVanished(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }

        redis.set(key(playerUuid), "1");
    }

    public void clear(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }

        redis.del(key(playerUuid));
    }

    public Set<String> getVanishedPlayersRaw() {
        return redis.keys(PREFIX + "*");
    }

    public UUID parseUuid(String redisKey) {
        if (redisKey == null || !redisKey.startsWith(PREFIX)) {
            return null;
        }

        try {
            return UUID.fromString(redisKey.substring(PREFIX.length()));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String key(UUID uuid) {
        return PREFIX + uuid;
    }
}
