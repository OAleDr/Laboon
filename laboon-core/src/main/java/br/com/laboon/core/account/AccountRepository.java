package br.com.laboon.core.account;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

import java.util.UUID;

public final class AccountRepository {

    private final JedisPooled redis;

    public AccountRepository(RedisManager redisManager) {
        this.redis = redisManager.getJedis();
    }

    public void save(Account account) {
        String key = "laboon:account:" + account.getUniqueId();

        redis.hset(key, "name", account.getName());
        redis.hset(key, "rank", account.getRank());
        redis.hset(key, "coins", String.valueOf(account.getCoins()));
        redis.hset(key, "experience", String.valueOf(account.getExperience()));
    }

    public Account findById(UUID uniqueId) {
        String key = "laboon:account:" + uniqueId;

        var data = redis.hgetAll(key);

        if (data.isEmpty()) {
            return null;
        }

        Account account = new Account(
                uniqueId,
                data.getOrDefault("name", "")
        );

        account.setRank(
                data.getOrDefault("rank", "DEFAULT")
        );

        account.setCoins(
                Long.parseLong(data.getOrDefault("coins", "0"))
        );

        account.setExperience(
                Long.parseLong(data.getOrDefault("experience", "0"))
        );

        return account;
    }

    public void delete(UUID uniqueId) {
        redis.del("laboon:account:" + uniqueId);
    }
}