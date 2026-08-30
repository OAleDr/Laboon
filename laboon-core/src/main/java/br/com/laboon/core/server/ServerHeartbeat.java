package br.com.laboon.core.server;

import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.redis.RedisManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ServerHeartbeat {

    private final ServerInfo server;
    private final ServerRegistry registry;


    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public ServerHeartbeat(
            ServerInfo server,
            RedisManager redisManager
    ) {
        this.server = server;
        this.registry = new ServerRegistry(redisManager);
    }

    public void start() {

        registry.register(server);

        scheduler.scheduleAtFixedRate(
                () -> registry.register(server),
                5,
                5,
                TimeUnit.SECONDS
        );
    }

    public void stop() {

        scheduler.shutdownNow();

        registry.unregister(server.getName());
    }
}