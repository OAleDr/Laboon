package br.com.laboon.core.account;

import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.redis.RedisManager;

import redis.clients.jedis.JedisPooled;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AccountRepository {

    private final JedisPooled redis;

    public AccountRepository(RedisManager redisManager) {
        this.redis = redisManager.getJedis();
    }

    public void save(Account account) {

        String key = "laboon:account:" + account.getUniqueId();

        AccountPreferences preferences = account.getPreferences();

        Map<String, String> data = new HashMap<>();

        data.put("name", account.getName());

        data.put("rank", account.getRank());

        data.put("coins", String.valueOf(account.getCoins()));

        data.put("experience", String.valueOf(account.getExperience()));

        data.put("type", account.getType().name());

        data.put("createdAt", account.getCreatedAt().toString());

        data.put("lastLogin", account.getLastLogin() == null ? "" : account.getLastLogin().toString());

        data.put("language", preferences.getLanguage().getCode());

        data.put("privateMessages", String.valueOf(preferences.isPrivateMessages()));

        data.put("friendRequests", String.valueOf(preferences.isFriendRequests()));

        data.put("serverJoinMessages", String.valueOf(preferences.isServerJoinMessages()));

        redis.hset(key, data);
    }

    public Account findById(UUID uniqueId) {

        String key = "laboon:account:" + uniqueId;

        Map<String, String> data = redis.hgetAll(key);

        if (data.isEmpty()) {
            return null;
        }

        Account account = new Account(uniqueId, data.getOrDefault("name", ""), AccountType.valueOf(data.getOrDefault("type", "ORIGINAL")), Instant.parse(data.getOrDefault("createdAt", Instant.now().toString())), parseInstant(data.get("lastLogin")), loadPreferences(data));

        account.setRank(data.getOrDefault("rank", "DEFAULT"));

        account.setCoins(Long.parseLong(data.getOrDefault("coins", "0")));

        account.setExperience(Long.parseLong(data.getOrDefault("experience", "0")));

        return account;
    }

    public boolean exists(UUID uniqueId) {

        return redis.exists("laboon:account:" + uniqueId);
    }

    public void delete(UUID uniqueId) {

        redis.del("laboon:account:" + uniqueId);
    }

    private AccountPreferences loadPreferences(Map<String, String> data) {

        LanguageLocale language;

        try {

            language = LanguageLocale.fromCode(data.getOrDefault("language", LanguageLocale.ptBR().getCode()));

        } catch (Exception exception) {

            language = LanguageLocale.ptBR();
        }

        return new AccountPreferences(language, Boolean.parseBoolean(data.getOrDefault("privateMessages", "true")), Boolean.parseBoolean(data.getOrDefault("friendRequests", "true")), Boolean.parseBoolean(data.getOrDefault("serverJoinMessages", "true")));
    }

    private Instant parseInstant(String value) {

        if (value == null || value.isBlank()) {

            return null;
        }

        try {

            return Instant.parse(value);

        } catch (Exception exception) {

            return null;
        }
    }
}