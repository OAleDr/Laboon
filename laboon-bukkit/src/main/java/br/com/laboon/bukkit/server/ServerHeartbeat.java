package br.com.laboon.bukkit.server;

import br.com.laboon.bukkit.config.ServerConfig;
import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.redis.RedisManager;
import br.com.laboon.core.server.*;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class ServerHeartbeat {

    private static final long HEARTBEAT_INTERVAL = 100L;
    private static final int SERVER_TTL = 15;

    private final JavaPlugin plugin;
    private final RedisManager redisManager;
    private final ServerConfig config;
    private final MessageBus messageBus;

    public ServerHeartbeat(
            JavaPlugin plugin,
            RedisManager redisManager,
            ServerConfig config,
            MessageBus messageBus
    ) {
        this.plugin = plugin;
        this.redisManager = redisManager;
        this.config = config;
        this.messageBus = messageBus;
    }

    public void start() {

        update();

        plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(
                        plugin,
                        this::update,
                        HEARTBEAT_INTERVAL,
                        HEARTBEAT_INTERVAL
                );
    }

    private void update() {

        ServerInfo server =
                createServerInfo();

        String key =
                "laboon:server:"
                        + server.getName();

        redisManager
                .getJedis()
                .hset(
                        key,
                        Map.of(
                                "name",
                                server.getName(),

                                "type",
                                server.getType().name(),

                                "role",
                                server.getRole().name(),

                                "state",
                                server.getState().name(),

                                "host",
                                server.getHost(),

                                "port",
                                String.valueOf(
                                        server.getPort()
                                ),

                                "players",
                                String.valueOf(
                                        server.getPlayers()
                                ),

                                "maxPlayers",
                                String.valueOf(
                                        server.getMaxPlayers()
                                )
                        )
                );

        redisManager
                .getJedis()
                .expire(
                        key,
                        SERVER_TTL
                );

        messageBus.publish(
                Channels.SERVER_INFO,
                ServerInfoSerializer.serialize(
                        server
                )
        );
    }

    private ServerInfo createServerInfo() {

        ServerType type =
                config.getServerType();

        ServerRole role =
                config.getServerRole();

        ServerInfo server =
                new ServerInfo(
                        config.getServerName(),
                        type,
                        role,
                        config.getHost(),
                        config.getPort(),
                        config.getMaxPlayers()
                );

        server.setPlayers(
                plugin.getServer()
                        .getOnlinePlayers()
                        .size()
        );

        server.setState(
                ServerState.ONLINE
        );

        return server;
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