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

        jedis.sadd(FRIENDS_PREFIX + first, second.toString());

        jedis.sadd(FRIENDS_PREFIX + second, first.toString());
    }

    public void removeFriend(UUID first, UUID second) {

        if (first == null || second == null) {
            return;
        }

        JedisPooled jedis = redisManager.getJedis();

        jedis.srem(FRIENDS_PREFIX + first, second.toString());

        jedis.srem(FRIENDS_PREFIX + second, first.toString());
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