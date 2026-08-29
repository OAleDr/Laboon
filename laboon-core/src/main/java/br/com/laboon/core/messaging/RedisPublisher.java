package br.com.laboon.core.messaging;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

public final class RedisPublisher {

    private final JedisPooled redis;

    public RedisPublisher(RedisManager redisManager) {
        this.redis = redisManager.getJedis();
    }

    public void publish(String channel, String message) {
        redis.publish(channel, message);
    }
}