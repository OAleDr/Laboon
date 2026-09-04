package br.com.laboon.core.account;

import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.redis.RedisManager;

import redis.clients.jedis.JedisPooled;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AccountRepository {

    private static final String ACCOUNT_KEY_PREFIX =
            "laboon:account:";

    private static final String ACCOUNT_NAME_KEY_PREFIX =
            "laboon:account:name:";

    private static final String TEMPORARY_GROUP_PREFIX =
            "temporaryGroup:";

    private final JedisPooled redis;

    public AccountRepository(RedisManager redisManager) {
        this.redis = redisManager.getJedis();
    }

    public void save(Account account) {

        String key =
                ACCOUNT_KEY_PREFIX
                        + account.getUniqueId();

        /*
         * Recupera o nome antigo antes de alterar
         * a conta. Isso permite atualizar o índice
         * caso o jogador tenha mudado de nome.
         */
        String previousName =
                redis.hget(key, "name");

        String currentName =
                normalizeName(account.getName());

        /*
         * Remove o índice antigo caso o nome tenha
         * sido alterado.
         */
        if (previousName != null
                && !previousName.isBlank()
                && !normalizeName(previousName)
                .equals(currentName)) {

            redis.del(
                    ACCOUNT_NAME_KEY_PREFIX
                            + normalizeName(previousName)
            );
        }

        AccountPreferences preferences =
                account.getPreferences();

        Map<String, String> data =
                new HashMap<>();

        data.put(
                "name",
                account.getName()
        );

        data.put(
                "group",
                account.getGroup().name()
        );

        data.put(
                "tag",
                account.getTag()
        );

        data.put(
                "experience",
                String.valueOf(
                        account.getExperience()
                )
        );

        data.put(
                "type",
                account.getType().name()
        );

        data.put(
                "createdAt",
                account.getCreatedAt().toString()
        );

        data.put(
                "lastLogin",
                account.getLastLogin() == null
                        ? ""
                        : account.getLastLogin().toString()
        );

        data.put(
                "language",
                preferences.getLanguage().getCode()
        );

        data.put(
                "privateMessages",
                String.valueOf(
                        preferences.isPrivateMessages()
                )
        );

        data.put(
                "friendRequests",
                String.valueOf(
                        preferences.isFriendRequests()
                )
        );

        data.put(
                "serverJoinMessages",
                String.valueOf(
                        preferences.isServerJoinMessages()
                )
        );

        redis.hset(
                key,
                data
        );

        /*
         * Atualiza o índice nome -> UUID.
         */
        redis.set(
                ACCOUNT_NAME_KEY_PREFIX
                        + currentName,
                account.getUniqueId().toString()
        );

        /*
         * Remove do Redis grupos temporários que
         * já não existem no Account.
         */
        Map<String, String> storedData =
                redis.hgetAll(key);

        for (String field :
                storedData.keySet()) {

            if (!field.startsWith(
                    TEMPORARY_GROUP_PREFIX
            )) {
                continue;
            }

            String groupName =
                    field.substring(
                            TEMPORARY_GROUP_PREFIX.length()
                    );

            Group group;

            try {

                group =
                        Group.valueOf(
                                groupName
                        );

            } catch (IllegalArgumentException exception) {

                redis.hdel(
                        key,
                        field
                );

                continue;
            }

            if (!account.getTemporaryGroups()
                    .containsKey(group)) {

                redis.hdel(
                        key,
                        field
                );
            }
        }

        /*
         * Salva os grupos temporários atuais.
         */
        for (Map.Entry<Group, Instant> entry :
                account.getTemporaryGroups()
                        .entrySet()) {

            Group group =
                    entry.getKey();

            Instant expiresAt =
                    entry.getValue();

            if (group == null
                    || expiresAt == null) {
                continue;
            }

            redis.hset(
                    key,
                    TEMPORARY_GROUP_PREFIX
                            + group.name(),
                    expiresAt.toString()
            );
        }
    }

    public Account findById(
            UUID uniqueId
    ) {

        if (uniqueId == null) {
            return null;
        }

        String key =
                ACCOUNT_KEY_PREFIX
                        + uniqueId;

        Map<String, String> data =
                redis.hgetAll(key);

        if (data.isEmpty()) {
            return null;
        }

        Account account =
                new Account(
                        uniqueId,
                        data.getOrDefault(
                                "name",
                                ""
                        ),
                        parseAccountType(
                                data.get("type")
                        ),
                        parseCreatedAt(
                                data.get("createdAt")
                        ),
                        parseInstant(
                                data.get("lastLogin")
                        ),
                        loadPreferences(data)
                );

        Group group =
                parseGroup(
                        data.get("group")
                );

        account.setGroup(group);

        String tag =
                data.get("tag");

        if (tag == null
                || tag.isBlank()) {

            tag =
                    group.getAbbreviation();
        }

        account.setTag(tag);

        account.setExperience(
                parseExperience(
                        data.get("experience")
                )
        );

        loadTemporaryGroups(
                account,
                data
        );

        return account;
    }

    /*
     * =========================
     * FIND BY NAME
     * =========================
     */

    public Account findByName(
            String name
    ) {

        if (name == null
                || name.isBlank()) {
            return null;
        }

        String normalizedName =
                normalizeName(name);

        String uuidValue =
                redis.get(
                        ACCOUNT_NAME_KEY_PREFIX
                                + normalizedName
                );

        if (uuidValue == null
                || uuidValue.isBlank()) {
            return null;
        }

        try {

            UUID uniqueId =
                    UUID.fromString(uuidValue);

            return findById(uniqueId);

        } catch (IllegalArgumentException exception) {

            /*
             * Índice inválido. Remove para não
             * deixar lixo permanente no Redis.
             */
            redis.del(
                    ACCOUNT_NAME_KEY_PREFIX
                            + normalizedName
            );

            return null;
        }
    }

    public boolean exists(
            UUID uniqueId
    ) {

        if (uniqueId == null) {
            return false;
        }

        return redis.exists(
                ACCOUNT_KEY_PREFIX
                        + uniqueId
        );
    }

    public void delete(
            UUID uniqueId
    ) {

        if (uniqueId == null) {
            return;
        }

        String key =
                ACCOUNT_KEY_PREFIX
                        + uniqueId;

        String name =
                redis.hget(
                        key,
                        "name"
                );

        /*
         * Remove também o índice do nome.
         */
        if (name != null
                && !name.isBlank()) {

            redis.del(
                    ACCOUNT_NAME_KEY_PREFIX
                            + normalizeName(name)
            );
        }

        redis.del(key);
    }

    /*
     * =========================
     * TEMPORARY GROUPS
     * =========================
     */

    private void loadTemporaryGroups(
            Account account,
            Map<String, String> data
    ) {

        Instant now =
                Instant.now();

        for (Map.Entry<String, String> entry :
                data.entrySet()) {

            String field =
                    entry.getKey();

            if (!field.startsWith(
                    TEMPORARY_GROUP_PREFIX
            )) {
                continue;
            }

            String groupName =
                    field.substring(
                            TEMPORARY_GROUP_PREFIX.length()
                    );

            Group group;

            try {

                group =
                        Group.valueOf(
                                groupName
                        );

            } catch (IllegalArgumentException exception) {

                continue;
            }

            Instant expiresAt =
                    parseInstant(
                            entry.getValue()
                    );

            if (expiresAt == null) {
                continue;
            }

            if (!expiresAt.isAfter(now)) {
                continue;
            }

            account.setTemporaryGroup(
                    group,
                    expiresAt
            );
        }
    }

    /*
     * =========================
     * PARSING
     * =========================
     */

    private Group parseGroup(
            String value
    ) {
        return Group.fromId(value);
    }

    private AccountType parseAccountType(
            String value
    ) {

        if (value == null
                || value.isBlank()) {
            return AccountType.ORIGINAL;
        }

        try {

            return AccountType.valueOf(
                    value
            );

        } catch (IllegalArgumentException exception) {

            return AccountType.ORIGINAL;
        }
    }

    private Instant parseCreatedAt(
            String value
    ) {

        Instant result =
                parseInstant(value);

        if (result == null) {
            return Instant.now();
        }

        return result;
    }

    private long parseExperience(
            String value
    ) {

        if (value == null
                || value.isBlank()) {
            return 0L;
        }

        try {

            return Long.parseLong(value);

        } catch (NumberFormatException exception) {

            return 0L;
        }
    }

    private AccountPreferences loadPreferences(
            Map<String, String> data
    ) {

        LanguageLocale language;

        try {

            language =
                    LanguageLocale.fromCode(
                            data.getOrDefault(
                                    "language",
                                    LanguageLocale
                                            .ptBR()
                                            .getCode()
                            )
                    );

        } catch (Exception exception) {

            language =
                    LanguageLocale.ptBR();
        }

        return new AccountPreferences(
                language,
                Boolean.parseBoolean(
                        data.getOrDefault(
                                "privateMessages",
                                "true"
                        )
                ),
                Boolean.parseBoolean(
                        data.getOrDefault(
                                "friendRequests",
                                "true"
                        )
                ),
                Boolean.parseBoolean(
                        data.getOrDefault(
                                "serverJoinMessages",
                                "true"
                        )
                )
        );
    }

    private Instant parseInstant(
            String value
    ) {

        if (value == null
                || value.isBlank()) {
            return null;
        }

        try {

            return Instant.parse(value);

        } catch (Exception exception) {

            return null;
        }
    }

    private String normalizeName(
            String name
    ) {

        return name.trim()
                .toLowerCase();
    }
}