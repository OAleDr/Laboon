package br.com.laboon.core.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisManagerTest {

    @Test
    void shouldConnectToRedis() {
        RedisManager redisManager =
                new RedisManager("localhost", 6379);

        var redis = redisManager.getJedis();

        redis.set("laboon:test", "funcionando");

        String value = redis.get("laboon:test");

        assertEquals("funcionando", value);

        redisManager.close();
    }
}