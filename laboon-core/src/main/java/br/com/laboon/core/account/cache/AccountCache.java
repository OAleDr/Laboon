package br.com.laboon.core.account.cache;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

import java.util.UUID;

/**
 * Cache de Accounts no Redis.
 *
 * PostgreSQL = persistência.
 * Redis = cache/runtime.
 */
public final class AccountCache {

    private static final String PREFIX =
            "laboon:account:";

    private static final String NAME_PREFIX =
            "laboon:account:name:";

    private final JedisPooled redis;
    private final AccountCacheCodec codec;

    public AccountCache(RedisManager redisManager) {

        if (redisManager == null) {
            throw new IllegalArgumentException(
                    "RedisManager não pode ser nulo."
            );
        }

        this.redis = redisManager.getJedis();
        this.codec = new AccountCacheCodec();
    }

    /**
     * Busca uma Account no Redis.
     */
    public Account get(UUID uuid) {

        if (uuid == null) {
            return null;
        }

        String key = PREFIX + uuid;

        String json = redis.get(key);

        if (json == null || json.isBlank()) {
            return null;
        }

        Account account = codec.deserialize(json);

        /*
         * Caso o JSON esteja corrompido,
         * remove a entrada inválida.
         */
        if (account == null) {
            redis.del(key);
        }

        return account;
    }

    /**
     * Coloca uma Account no cache.
     */
    public void put(Account account) {

        if (account == null || account.getUniqueId() == null) {
            return;
        }

        String uuid = account.getUniqueId().toString();

        String key = PREFIX + uuid;

        String newName = normalizeName(account.getName());

        /*
         * Recuperamos o valor antigo do JSON
         * antes de substituir a entrada.
         *
         * Isso permite limpar o índice antigo
         * quando o jogador muda de nome.
         */
        String previousJson = redis.get(key);

        if (previousJson != null && !previousJson.isBlank()) {

            Account previousAccount =
                    codec.deserialize(previousJson);

            if (previousAccount != null) {

                String previousName =
                        normalizeName(previousAccount.getName());

                if (!previousName.isBlank()
                        && !previousName.equals(newName)) {

                    String previousNameKey =
                            NAME_PREFIX + previousName;

                    String indexedUuid =
                            redis.get(previousNameKey);

                    /*
                     * Só removemos se o índice ainda
                     * apontar para este jogador.
                     */
                    if (uuid.equals(indexedUuid)) {
                        redis.del(previousNameKey);
                    }
                }
            }
        }

        String json = codec.serialize(account);

        redis.set(
                key,
                json
        );

        /*
         * Índice:
         *
         * nome normalizado -> UUID
         */
        if (!newName.isBlank()) {

            redis.set(
                    NAME_PREFIX + newName,
                    uuid
            );
        }
    }

    /**
     * Busca o UUID através do índice de nome.
     */
    public UUID findUuidByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        String normalizedName =
                normalizeName(name);

        String value =
                redis.get(
                        NAME_PREFIX + normalizedName
                );

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            UUID uuid = UUID.fromString(value);

            /*
             * Verifica se o cache realmente contém
             * a conta correspondente.
             *
             * Caso contrário o índice está órfão.
             */
            if (!redis.exists(PREFIX + uuid)) {
                redis.del(
                        NAME_PREFIX + normalizedName
                );

                return null;
            }

            return uuid;

        } catch (IllegalArgumentException exception) {

            redis.del(
                    NAME_PREFIX + normalizedName
            );

            return null;
        }
    }

    /**
     * Remove uma Account do cache.
     */
    public void remove(Account account) {

        if (account == null) {
            return;
        }

        remove(
                account.getUniqueId(),
                account.getName()
        );
    }

    /**
     * Remove uma Account pelo UUID e nome.
     */
    public void remove(
            UUID uuid,
            String name
    ) {

        if (uuid == null) {
            return;
        }

        redis.del(
                PREFIX + uuid
        );

        if (name != null && !name.isBlank()) {

            String normalizedName =
                    normalizeName(name);

            String key =
                    NAME_PREFIX + normalizedName;

            String indexedUuid =
                    redis.get(key);

            /*
             * Só remove o índice se ele realmente
             * pertence a este UUID.
             */
            if (uuid.toString().equals(indexedUuid)) {
                redis.del(key);
            }
        }
    }

    /**
     * Verifica se a Account está no cache.
     */
    public boolean contains(UUID uuid) {

        if (uuid == null) {
            return false;
        }

        return redis.exists(
                PREFIX + uuid
        );
    }

    /**
     * Limpa o cache de uma Account.
     *
     * Útil quando precisamos invalidar
     * explicitamente uma conta.
     */
    public void invalidate(UUID uuid) {

        if (uuid == null) {
            return;
        }

        String key =
                PREFIX + uuid;

        String json =
                redis.get(key);

        if (json != null && !json.isBlank()) {

            Account account =
                    codec.deserialize(json);

            if (account != null) {

                remove(account);

                return;
            }
        }

        redis.del(key);
    }

    private String normalizeName(String name) {

        if (name == null) {
            return "";
        }

        return name
                .trim()
                .toLowerCase();
    }
}