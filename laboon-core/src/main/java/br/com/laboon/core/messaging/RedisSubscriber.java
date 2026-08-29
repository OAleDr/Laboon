package br.com.laboon.core.messaging;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;

public final class RedisSubscriber {

    private final JedisPooled redis;

    public RedisSubscriber(RedisManager redisManager) {
        this.redis = redisManager.getJedis();
    }

    public void subscribe(String channel, MessageHandler handler) {

        Thread thread = new Thread(() -> {

            redis.subscribe(new JedisPubSub() {

                @Override
                public void onMessage(String receivedChannel, String message) {
                    handler.handle(receivedChannel, message);
                }

            }, channel);

        }, "laboon-redis-subscriber");

        thread.setDaemon(true);
        thread.start();
    }
}