package br.com.laboon.core.account;

import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.account.punishment.Ban;
import br.com.laboon.core.account.punishment.Kick;
import br.com.laboon.core.account.punishment.Mute;
import br.com.laboon.core.account.punishment.PunishmentHistory;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.redis.RedisManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import redis.clients.jedis.JedisPooled;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AccountRepository {

    private static final String ACCOUNT_KEY_PREFIX = "laboon:account:";

    private static final String ACCOUNT_NAME_KEY_PREFIX = "laboon:account:name:";

    private static final String TEMPORARY_GROUP_PREFIX = "temporaryGroup:";

    private static final String PUNISHMENT_HISTORY_FIELD = "punishmentHistory";

    private final JedisPooled redis;

    public AccountRepository(RedisManager redisManager) {
        this.redis = redisManager.getJedis();
    }

    /*
     * =========================
     * SAVE
     * =========================
     */

    public void save(Account account) {

        String key = ACCOUNT_KEY_PREFIX + account.getUniqueId();

        /*
         * Recupera o nome antigo antes de alterar
         * a conta. Isso permite atualizar o índice
         * caso o jogador tenha mudado de nome.
         */
        String previousName = redis.hget(key, "name");

        String currentName = normalizeName(account.getName());

        /*
         * Remove o índice antigo caso o nome tenha
         * sido alterado.
         */
        if (previousName != null && !previousName.isBlank() && !normalizeName(previousName).equals(currentName)) {

            redis.del(ACCOUNT_NAME_KEY_PREFIX + normalizeName(previousName));
        }

        AccountPreferences preferences = account.getPreferences();

        Map<String, String> data = new HashMap<>();

        data.put("name", account.getName());

        data.put("group", account.getGroup().name());

        data.put("tag", account.getTag());

        data.put("experience", String.valueOf(account.getExperience()));

        data.put("type", account.getType().name());

        data.put("createdAt", account.getCreatedAt().toString());

        data.put("lastLogin", account.getLastLogin() == null ? "" : account.getLastLogin().toString());

        data.put("language", preferences.getLanguage().getCode());

        data.put("privateMessages", String.valueOf(preferences.isPrivateMessages()));

        data.put("friendRequests", String.valueOf(preferences.isFriendRequests()));

        data.put("serverJoinMessages", String.valueOf(preferences.isServerJoinMessages()));

        /*
         * =========================
         * PUNIÇÕES
         * =========================
         */

        data.put(PUNISHMENT_HISTORY_FIELD, serializePunishmentHistory(account.getPunishmentHistory()));

        redis.hset(key, data);

        /*
         * Atualiza o índice nome -> UUID.
         */
        redis.set(ACCOUNT_NAME_KEY_PREFIX + currentName, account.getUniqueId().toString());

        /*
         * =========================
         * GRUPOS TEMPORÁRIOS
         * =========================
         */

        Map<String, String> storedData = redis.hgetAll(key);

        /*
         * Remove grupos temporários que não existem
         * mais no Account.
         */
        for (String field : storedData.keySet()) {

            if (!field.startsWith(TEMPORARY_GROUP_PREFIX)) {
                continue;
            }

            String groupName = field.substring(TEMPORARY_GROUP_PREFIX.length());

            Group group;

            try {
                group = Group.valueOf(groupName);

            } catch (IllegalArgumentException exception) {

                redis.hdel(key, field);

                continue;
            }

            if (!account.getTemporaryGroups().containsKey(group)) {

                redis.hdel(key, field);
            }
        }

        /*
         * Salva os grupos temporários atuais.
         */
        for (Map.Entry<Group, Instant> entry : account.getTemporaryGroups().entrySet()) {

            Group group = entry.getKey();

            Instant expiresAt = entry.getValue();

            if (group == null || expiresAt == null) {
                continue;
            }

            redis.hset(key, TEMPORARY_GROUP_PREFIX + group.name(), expiresAt.toString());
        }
    }

    /*
     * =========================
     * FIND BY ID
     * =========================
     */

    public Account findById(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        String key = ACCOUNT_KEY_PREFIX + uniqueId;

        Map<String, String> data = redis.hgetAll(key);

        if (data.isEmpty()) {
            return null;
        }

        /*
         * IMPORTANTE:
         *
         * Este é o construtor REAL da sua Account.
         */
        Account account = new Account(uniqueId, data.getOrDefault("name", ""), parseAccountType(data.get("type")), parseCreatedAt(data.get("createdAt")), parseInstant(data.get("lastLogin")), loadPreferences(data));

        /*
         * Grupo permanente.
         */
        Group group = parseGroup(data.get("group"));

        account.setGroup(group);

        /*
         * Tag.
         */
        String tag = data.get("tag");

        if (tag == null || tag.isBlank()) {

            tag = group.getAbbreviation();
        }

        account.setTag(tag);

        /*
         * Experiência.
         */
        account.setExperience(parseExperience(data.get("experience")));

        /*
         * Grupos temporários.
         */
        loadTemporaryGroups(account, data);

        /*
         * Histórico de punições.
         */
        loadPunishmentHistory(account, data.get(PUNISHMENT_HISTORY_FIELD));

        return account;
    }

    /*
     * =========================
     * FIND BY NAME
     * =========================
     */

    public Account findByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        String normalizedName = normalizeName(name);

        String uuidValue = redis.get(ACCOUNT_NAME_KEY_PREFIX + normalizedName);

        if (uuidValue == null || uuidValue.isBlank()) {
            return null;
        }

        try {

            UUID uniqueId = UUID.fromString(uuidValue);

            return findById(uniqueId);

        } catch (IllegalArgumentException exception) {

            /*
             * Índice inválido.
             */
            redis.del(ACCOUNT_NAME_KEY_PREFIX + normalizedName);

            return null;
        }
    }

    /*
     * =========================
     * EXISTS
     * =========================
     */

    public boolean exists(UUID uniqueId) {

        if (uniqueId == null) {
            return false;
        }

        return redis.exists(ACCOUNT_KEY_PREFIX + uniqueId);
    }

    /*
     * =========================
     * DELETE
     * =========================
     */

    public void delete(UUID uniqueId) {

        if (uniqueId == null) {
            return;
        }

        String key = ACCOUNT_KEY_PREFIX + uniqueId;

        String name = redis.hget(key, "name");

        /*
         * Remove também o índice do nome.
         */
        if (name != null && !name.isBlank()) {

            redis.del(ACCOUNT_NAME_KEY_PREFIX + normalizeName(name));
        }

        redis.del(key);
    }

    /*
     * =========================
     * TEMPORARY GROUPS
     * =========================
     */

    private void loadTemporaryGroups(Account account, Map<String, String> data) {

        Instant now = Instant.now();

        for (Map.Entry<String, String> entry : data.entrySet()) {

            String field = entry.getKey();

            if (!field.startsWith(TEMPORARY_GROUP_PREFIX)) {
                continue;
            }

            String groupName = field.substring(TEMPORARY_GROUP_PREFIX.length());

            Group group;

            try {

                group = Group.valueOf(groupName);

            } catch (IllegalArgumentException exception) {
                continue;
            }

            Instant expiresAt = parseInstant(entry.getValue());

            if (expiresAt == null) {
                continue;
            }

            /*
             * Grupo já expirado.
             */
            if (!expiresAt.isAfter(now)) {
                continue;
            }

            account.setTemporaryGroup(group, expiresAt);
        }
    }

    /*
     * =========================
     * PUNISHMENT SERIALIZATION
     * =========================
     */

    private String serializePunishmentHistory(PunishmentHistory history) {

        if (history == null) {
            return new JsonObject().toString();
        }

        JsonObject root = new JsonObject();

        /*
         * =========================
         * BANS
         * =========================
         */

        JsonArray bans = new JsonArray();

        for (Ban ban : history.getBanHistory()) {

            if (ban == null) {
                continue;
            }

            JsonObject object = new JsonObject();

            object.addProperty("bannedBy", ban.getBannedBy());

            object.addProperty("bannedByUniqueId", ban.getBannedByUniqueId().toString());

            if (ban.getBannedIp() != null) {

                object.addProperty("bannedIp", ban.getBannedIp());
            }

            if (ban.getServer() != null) {

                object.addProperty("server", ban.getServer());
            }

            object.addProperty("banTime", ban.getBanTime().toString());

            object.addProperty("reason", ban.getReason());

            if (ban.getExpire() != null) {

                object.addProperty("expire", ban.getExpire().toString());
            }

            object.addProperty("unbanned", ban.isUnbanned());

            if (ban.getUnbannedBy() != null) {

                object.addProperty("unbannedBy", ban.getUnbannedBy());
            }

            if (ban.getUnbannedByUniqueId() != null) {

                object.addProperty("unbannedByUniqueId", ban.getUnbannedByUniqueId().toString());
            }

            if (ban.getUnbanTime() != null) {

                object.addProperty("unbanTime", ban.getUnbanTime().toString());
            }

            bans.add(object);
        }

        root.add("bans", bans);

        /*
         * =========================
         * MUTES
         * =========================
         */

        JsonArray mutes = new JsonArray();

        for (Mute mute : history.getMuteHistory()) {

            if (mute == null) {
                continue;
            }

            JsonObject object = new JsonObject();

            object.addProperty("mutedBy", mute.getMutedBy());

            object.addProperty("mutedByUniqueId", mute.getMutedByUniqueId().toString());

            if (mute.getMutedIp() != null) {

                object.addProperty("mutedIp", mute.getMutedIp());
            }

            if (mute.getServer() != null) {

                object.addProperty("server", mute.getServer());
            }

            object.addProperty("muteTime", mute.getMuteTime().toString());

            object.addProperty("reason", mute.getReason());

            if (mute.getExpire() != null) {

                object.addProperty("expire", mute.getExpire().toString());
            }

            object.addProperty("unmuted", mute.isUnmuted());

            if (mute.getUnmutedBy() != null) {

                object.addProperty("unmutedBy", mute.getUnmutedBy());
            }

            if (mute.getUnmutedByUniqueId() != null) {

                object.addProperty("unmutedByUniqueId", mute.getUnmutedByUniqueId().toString());
            }

            if (mute.getUnmuteTime() != null) {

                object.addProperty("unmuteTime", mute.getUnmuteTime().toString());
            }

            mutes.add(object);
        }

        root.add("mutes", mutes);

        /*
         * =========================
         * KICKS
         * =========================
         */

        JsonArray kicks = new JsonArray();

        for (Kick kick : history.getKickHistory()) {

            if (kick == null) {
                continue;
            }

            JsonObject object = new JsonObject();

            object.addProperty("kickedBy", kick.getKickedBy());

            object.addProperty("kickedByUniqueId", kick.getKickedByUniqueId().toString());

            if (kick.getServer() != null) {

                object.addProperty("server", kick.getServer());
            }

            object.addProperty("time", kick.getTime().toString());

            object.addProperty("reason", kick.getReason());

            kicks.add(object);
        }

        root.add("kicks", kicks);

        return root.toString();
    }

    /*
     * =========================
     * PUNISHMENT LOAD
     * =========================
     */

    private void loadPunishmentHistory(Account account, String json) {

        if (json == null || json.isBlank()) {
            return;
        }

        try {

            JsonElement parsed = JsonParser.parseString(json);

            if (!parsed.isJsonObject()) {
                return;
            }

            JsonObject root = parsed.getAsJsonObject();

            PunishmentHistory history = account.getPunishmentHistory();

            /*
             * =========================
             * BANS
             * =========================
             */

            JsonArray bans = getArray(root, "bans");

            for (JsonElement element : bans) {

                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject object = element.getAsJsonObject();

                String bannedBy = getString(object, "bannedBy");

                UUID bannedByUniqueId = getUUID(object, "bannedByUniqueId");

                String bannedIp = getString(object, "bannedIp");

                String server = getString(object, "server");

                Instant banTime = getInstant(object, "banTime");

                String reason = getString(object, "reason");

                Instant expire = getInstant(object, "expire");

                if (bannedBy == null || bannedByUniqueId == null || banTime == null || reason == null) {
                    continue;
                }

                boolean unbanned = getBoolean(object, "unbanned");

                String unbannedBy = getString(object, "unbannedBy");

                UUID unbannedByUniqueId = getUUID(object, "unbannedByUniqueId");

                Instant unbanTime = getInstant(object, "unbanTime");

                try {

                    Ban ban = Ban.restore(bannedBy, bannedByUniqueId, bannedIp, server, banTime, reason, expire, unbanned, unbannedBy, unbannedByUniqueId, unbanTime);

                    history.addBan(ban);

                } catch (IllegalArgumentException ignored) {
                    /*
                     * Punição inválida não impede
                     * o carregamento da conta.
                     */
                }
            }

            /*
             * =========================
             * MUTES
             * =========================
             */

            JsonArray mutes = getArray(root, "mutes");

            for (JsonElement element : mutes) {

                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject object = element.getAsJsonObject();

                String mutedBy = getString(object, "mutedBy");

                UUID mutedByUniqueId = getUUID(object, "mutedByUniqueId");

                String mutedIp = getString(object, "mutedIp");

                String server = getString(object, "server");

                Instant muteTime = getInstant(object, "muteTime");

                String reason = getString(object, "reason");

                Instant expire = getInstant(object, "expire");

                if (mutedBy == null || mutedByUniqueId == null || muteTime == null || reason == null) {
                    continue;
                }

                boolean unmuted = getBoolean(object, "unmuted");

                String unmutedBy = getString(object, "unmutedBy");

                UUID unmutedByUniqueId = getUUID(object, "unmutedByUniqueId");

                Instant unmuteTime = getInstant(object, "unmuteTime");

                try {

                    Mute mute = Mute.restore(mutedBy, mutedByUniqueId, mutedIp, server, muteTime, reason, expire, unmuted, unmutedBy, unmutedByUniqueId, unmuteTime);

                    history.addMute(mute);

                } catch (IllegalArgumentException ignored) {
                    /*
                     * Punição inválida não impede
                     * o carregamento da conta.
                     */
                }
            }

            /*
             * =========================
             * KICKS
             * =========================
             */

            JsonArray kicks = getArray(root, "kicks");

            for (JsonElement element : kicks) {

                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject object = element.getAsJsonObject();

                String kickedBy = getString(object, "kickedBy");

                UUID kickedByUniqueId = getUUID(object, "kickedByUniqueId");

                String server = getString(object, "server");

                Instant time = getInstant(object, "time");

                String reason = getString(object, "reason");

                if (kickedBy == null || kickedByUniqueId == null || time == null || reason == null) {
                    continue;
                }

                try {

                    Kick kick = new Kick(kickedBy, kickedByUniqueId, server, time, reason);

                    history.addKick(kick);

                } catch (IllegalArgumentException ignored) {
                    /*
                     * Punição inválida não impede
                     * o carregamento da conta.
                     */
                }
            }

        } catch (Exception ignored) {
            /*
             * JSON inválido não impede o carregamento
             * da conta.
             */
        }
    }

    /*
     * =========================
     * JSON HELPERS
     * =========================
     */

    private JsonArray getArray(JsonObject object, String property) {

        if (!object.has(property)) {
            return new JsonArray();
        }

        JsonElement element = object.get(property);

        if (element == null || !element.isJsonArray()) {
            return new JsonArray();
        }

        return element.getAsJsonArray();
    }

    private String getString(JsonObject object, String property) {

        if (!object.has(property)) {
            return null;
        }

        JsonElement element = object.get(property);

        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return null;
        }

        return element.getAsString();
    }

    private boolean getBoolean(JsonObject object, String property) {

        if (!object.has(property)) {
            return false;
        }

        JsonElement element = object.get(property);

        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return false;
        }

        try {

            return element.getAsBoolean();

        } catch (Exception exception) {

            return false;
        }
    }

    private UUID getUUID(JsonObject object, String property) {

        String value = getString(object, property);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return UUID.fromString(value);

        } catch (IllegalArgumentException exception) {

            return null;
        }
    }

    private Instant getInstant(JsonObject object, String property) {

        String value = getString(object, property);

        return parseInstant(value);
    }

    /*
     * =========================
     * PARSING
     * =========================
     */

    private Group parseGroup(String value) {

        return Group.fromId(value);
    }

    private AccountType parseAccountType(String value) {

        if (value == null || value.isBlank()) {

            return AccountType.ORIGINAL;
        }

        try {

            return AccountType.valueOf(value);

        } catch (IllegalArgumentException exception) {

            return AccountType.ORIGINAL;
        }
    }

    private Instant parseCreatedAt(String value) {

        Instant result = parseInstant(value);

        if (result == null) {
            return Instant.now();
        }

        return result;
    }

    private long parseExperience(String value) {

        if (value == null || value.isBlank()) {
            return 0L;
        }

        try {

            return Long.parseLong(value);

        } catch (NumberFormatException exception) {

            return 0L;
        }
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

    private String normalizeName(String name) {

        return name.trim().toLowerCase();
    }
}