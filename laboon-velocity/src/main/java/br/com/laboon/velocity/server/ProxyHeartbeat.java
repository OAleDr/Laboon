package br.com.laboon.velocity.server;

import br.com.laboon.core.redis.RedisManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ProxyHeartbeat {

    private final RedisManager redisManager;
    private final String proxyName;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public ProxyHeartbeat(
            RedisManager redisManager,
            String proxyName
    ) {
        this.redisManager = redisManager;
        this.proxyName = proxyName;
    }

    public void start() {

        update();

        scheduler.scheduleAtFixedRate(
                this::update,
                5,
                5,
                TimeUnit.SECONDS
        );
    }

    private void update() {

        String key =
                "laboon:proxy:" + proxyName;

        redisManager
                .getJedis()
                .hset(
                        key,
                        "name",
                        proxyName
                );

        redisManager
                .getJedis()
                .hset(
                        key,
                        "state",
                        "ONLINE"
                );

        redisManager
                .getJedis()
                .expire(
                        key,
                        15
                );
    }

    public void stop() {

        scheduler.shutdownNow();

        redisManager
                .getJedis()
                .del(
                        "laboon:proxy:" + proxyName
                );
    }
}