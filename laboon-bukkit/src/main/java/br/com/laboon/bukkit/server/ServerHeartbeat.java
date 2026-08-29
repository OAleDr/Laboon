package br.com.laboon.bukkit.server;

import br.com.laboon.bukkit.config.ServerConfig;
import br.com.laboon.core.redis.RedisManager;

import org.bukkit.plugin.java.JavaPlugin;

public final class ServerHeartbeat {

    private final JavaPlugin plugin;
    private final RedisManager redisManager;
    private final ServerConfig config;

    public ServerHeartbeat(
            JavaPlugin plugin,
            RedisManager redisManager,
            ServerConfig config
    ) {
        this.plugin = plugin;
        this.redisManager = redisManager;
        this.config = config;
    }

    public void start() {

        update();

        plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(
                        plugin,
                        this::update,
                        100L,
                        100L
                );
    }

    private void update() {

        String key =
                "laboon:server:"
                        + config.getServerName();

        redisManager
                .getJedis()
                .hset(
                        key,
                        "name",
                        config.getServerName()
                );

        redisManager
                .getJedis()
                .hset(
                        key,
                        "type",
                        config.getServerType()
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
                .hset(
                        key,
                        "players",
                        String.valueOf(
                                plugin.getServer()
                                        .getOnlinePlayers()
                                        .size()
                        )
                );

        redisManager
                .getJedis()
                .expire(
                        key,
                        15
                );
    }

    public void stop() {

        redisManager
                .getJedis()
                .del(
                        "laboon:server:"
                                + config.getServerName()
                );
    }
}