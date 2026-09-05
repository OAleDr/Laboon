package br.com.laboon.core.friend;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FriendRepository {

    private static final String FRIENDS_PREFIX = "laboon:friends:";

    private static final String FRIENDS_ADDED_PREFIX = "laboon:friends:added:";

    private static final String REQUEST_PREFIX = "laboon:friend:requests:";

    private final RedisManager redisManager;

    public FriendRepository(RedisManager redisManager) {

        if (redisManager == null) {
            throw new IllegalArgumentException("RedisManager não pode ser nulo.");
        }

        this.redisManager = redisManager;
    }

    /*
     * =========================
     * AMIGOS
     * =========================
     */

    public void addFriend(UUID first, UUID second) {

        if (first == null || second == null) {
            return;
        }

        if (first.equals(second)) {
            return;
        }

        JedisPooled jedis = redisManager.getJedis();

        String firstKey = FRIENDS_PREFIX + first;

        String secondKey = FRIENDS_PREFIX + second;

        String firstAddedKey = FRIENDS_ADDED_PREFIX + first;

        String secondAddedKey = FRIENDS_ADDED_PREFIX + second;

        Instant now = Instant.now();

        /*
         * Adiciona os amigos ao SET.
         */

        jedis.sadd(firstKey, second.toString());

        jedis.sadd(secondKey, first.toString());

        /*
         * Guarda a data somente se ainda
         * não existir.
         *
         * Isso evita alterar a data original
         * caso addFriend seja chamado novamente.
         */

        if (!jedis.hexists(firstAddedKey, second.toString())) {

            jedis.hset(firstAddedKey, second.toString(), now.toString());
        }

        if (!jedis.hexists(secondAddedKey, first.toString())) {

            jedis.hset(secondAddedKey, first.toString(), now.toString());
        }
    }

    public void removeFriend(UUID first, UUID second) {

        if (first == null || second == null) {
            return;
        }

        JedisPooled jedis = redisManager.getJedis();

        jedis.srem(FRIENDS_PREFIX + first, second.toString());

        jedis.srem(FRIENDS_PREFIX + second, first.toString());

        /*
         * Remove também a data de amizade.
         */

        jedis.hdel(FRIENDS_ADDED_PREFIX + first, second.toString());

        jedis.hdel(FRIENDS_ADDED_PREFIX + second, first.toString());
    }

    public boolean isFriend(UUID first, UUID second) {

        if (first == null || second == null) {
            return false;
        }

        JedisPooled jedis = redisManager.getJedis();

        return jedis.sismember(FRIENDS_PREFIX + first, second.toString());
    }

    public Set<UUID> getFriends(UUID uniqueId) {

        Set<UUID> friends = new HashSet<>();

        if (uniqueId == null) {
            return friends;
        }

        JedisPooled jedis = redisManager.getJedis();

        Set<String> values = jedis.smembers(FRIENDS_PREFIX + uniqueId);

        for (String value : values) {

            try {

                friends.add(UUID.fromString(value));

            } catch (IllegalArgumentException ignored) {
                // UUID inválido no Redis.
            }
        }

        return friends;
    }

    /**
     * Retorna os amigos com a data de adição.
     * <p>
     * Para amizades antigas, criadas antes da
     * implementação da persistência da data,
     * a data será registrada no momento da
     * primeira leitura.
     */
    public List<Friend> getFriendDetails(UUID uniqueId) {

        List<Friend> friends = new ArrayList<>();

        if (uniqueId == null) {
            return friends;
        }

        JedisPooled jedis = redisManager.getJedis();

        String friendsKey = FRIENDS_PREFIX + uniqueId;

        String addedKey = FRIENDS_ADDED_PREFIX + uniqueId;

        Set<String> values = jedis.smembers(friendsKey);

        for (String value : values) {

            try {

                UUID friendId = UUID.fromString(value);

                String addedAtValue = jedis.hget(addedKey, friendId.toString());

                Instant addedAt;

                if (addedAtValue == null || addedAtValue.isBlank()) {

                    /*
                     * Amizade antiga sem data.
                     *
                     * Não temos como recuperar a data
                     * verdadeira, então registramos agora
                     * para que daqui em diante ela fique
                     * persistida.
                     */

                    addedAt = Instant.now();

                    jedis.hset(addedKey, friendId.toString(), addedAt.toString());

                } else {

                    try {

                        addedAt = Instant.parse(addedAtValue);

                    } catch (Exception exception) {

                        addedAt = Instant.now();

                        jedis.hset(addedKey, friendId.toString(), addedAt.toString());
                    }
                }

                friends.add(new Friend(friendId, addedAt));

            } catch (IllegalArgumentException ignored) {
                // UUID inválido no Redis.
            }
        }

        return friends;
    }

    /**
     * Retorna um amigo específico com sua
     * data de adição.
     */
    public Friend getFriend(UUID owner, UUID friendId) {

        if (owner == null || friendId == null) {
            return null;
        }

        if (!isFriend(owner, friendId)) {
            return null;
        }

        JedisPooled jedis = redisManager.getJedis();

        String key = FRIENDS_ADDED_PREFIX + owner;

        String value = jedis.hget(key, friendId.toString());

        Instant addedAt;

        if (value == null || value.isBlank()) {

            addedAt = Instant.now();

            jedis.hset(key, friendId.toString(), addedAt.toString());

        } else {

            try {

                addedAt = Instant.parse(value);

            } catch (Exception exception) {

                addedAt = Instant.now();

                jedis.hset(key, friendId.toString(), addedAt.toString());
            }
        }

        return new Friend(friendId, addedAt);
    }

    /*
     * =========================
     * SOLICITAÇÕES
     * =========================
     */

    public void saveRequest(FriendRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Solicitação não pode ser nula.");
        }

        JedisPooled jedis = redisManager.getJedis();

        String key = REQUEST_PREFIX + request.getReceiver();

        jedis.hset(key, request.getSender().toString(), request.getCreatedAt().toString());
    }

    public FriendRequest getRequest(UUID receiver, UUID sender) {

        if (receiver == null || sender == null) {
            return null;
        }

        JedisPooled jedis = redisManager.getJedis();

        String value = jedis.hget(REQUEST_PREFIX + receiver, sender.toString());

        if (value == null) {
            return null;
        }

        try {

            return new FriendRequest(sender, receiver, Instant.parse(value));

        } catch (Exception exception) {

            jedis.hdel(REQUEST_PREFIX + receiver, sender.toString());

            return null;
        }
    }

    public List<FriendRequest> getRequests(UUID receiver) {

        List<FriendRequest> requests = new ArrayList<>();

        if (receiver == null) {
            return requests;
        }

        JedisPooled jedis = redisManager.getJedis();

        Map<String, String> values = jedis.hgetAll(REQUEST_PREFIX + receiver);

        for (Map.Entry<String, String> entry : values.entrySet()) {

            try {

                UUID sender = UUID.fromString(entry.getKey());

                Instant createdAt = Instant.parse(entry.getValue());

                requests.add(new FriendRequest(sender, receiver, createdAt));

            } catch (Exception ignored) {
                // Solicitação inválida.
            }
        }

        return requests;
    }

    public boolean hasRequest(UUID receiver, UUID sender) {

        if (receiver == null || sender == null) {
            return false;
        }

        JedisPooled jedis = redisManager.getJedis();

        return jedis.hexists(REQUEST_PREFIX + receiver, sender.toString());
    }

    public void removeRequest(UUID receiver, UUID sender) {

        if (receiver == null || sender == null) {
            return;
        }

        JedisPooled jedis = redisManager.getJedis();

        jedis.hdel(REQUEST_PREFIX + receiver, sender.toString());
    }

    public List<FriendRequest> getOutgoingRequests(UUID sender) {

        List<FriendRequest> requests = new ArrayList<>();

        if (sender == null) {
            return requests;
        }

        JedisPooled jedis = redisManager.getJedis();

        Set<String> keys = jedis.keys(REQUEST_PREFIX + "*");

        for (String key : keys) {

            String receiverValue = key.substring(REQUEST_PREFIX.length());

            try {

                UUID receiver = UUID.fromString(receiverValue);

                String createdAtValue = jedis.hget(key, sender.toString());

                if (createdAtValue == null) {
                    continue;
                }

                Instant createdAt = Instant.parse(createdAtValue);

                requests.add(new FriendRequest(sender, receiver, createdAt));

            } catch (Exception ignored) {
                // Ignora solicitação inválida.
            }
        }

        return requests;
    }
}