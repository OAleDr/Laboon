package br.com.laboon.core.account.cache;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.redis.RedisManager;

import redis.clients.jedis.JedisPooled;

import java.util.UUID;

public final class AccountCache {

    private static final String PREFIX =
            "laboon:account:";

    private static final String NAME_PREFIX =
            "laboon:account:name:";

    private final JedisPooled redis;
    private final AccountCacheCodec codec;

    public AccountCache(
            RedisManager redisManager
    ) {

        if (redisManager == null) {
            throw new IllegalArgumentException(
                    "RedisManager não pode ser nulo."
            );
        }

        this.redis = redisManager.getJedis();
        this.codec = new AccountCacheCodec();
    }

    public Account get(UUID uuid) {

        if (uuid == null) {
            return null;
        }

        String json =
                redis.get(PREFIX + uuid);

        if (json == null || json.isBlank()) {
            return null;
        }

        return codec.deserialize(json);
    }

    public void put(Account account) {

        if (account == null) {
            return;
        }

        String json =
                codec.serialize(account);

        redis.set(
                PREFIX + account.getUniqueId(),
                json
        );

        redis.set(
                NAME_PREFIX
                        + normalizeName(account.getName()),
                account.getUniqueId().toString()
        );
    }

    public UUID findUuidByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        String value =
                redis.get(
                        NAME_PREFIX
                                + normalizeName(name)
                );

        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public void remove(Account account) {

        if (account == null) {
            return;
        }

        remove(account.getUniqueId(), account.getName());
    }

    public void remove(UUID uuid, String name) {

        if (uuid == null) {
            return;
        }

        redis.del(PREFIX + uuid);

        if (name != null && !name.isBlank()) {

            redis.del(
                    NAME_PREFIX
                            + normalizeName(name)
            );
        }
    }

    private String normalizeName(String name) {

        return name.trim().toLowerCase();
    }
}