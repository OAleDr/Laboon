package br.com.laboon.core.redis;

import redis.clients.jedis.JedisPooled;

public final class RedisManager {

    private final JedisPooled jedis;

    public RedisManager(String host, int port) {
        this.jedis = new JedisPooled(host, port);
    }

    public JedisPooled getJedis() {
        return jedis;
    }

    public boolean isConnected() {
        try {
            String key = "laboon:connection:test";

            jedis.setex(key, 10, "OK");

            return "OK".equals(jedis.get(key));

        } catch (Exception exception) {
            return false;
        }
    }

    public void close() {
        jedis.close();
    }
}