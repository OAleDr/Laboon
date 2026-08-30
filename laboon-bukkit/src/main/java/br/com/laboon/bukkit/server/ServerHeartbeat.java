package br.com.laboon.bukkit.server;

import br.com.laboon.bukkit.config.ServerConfig;
import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.redis.RedisManager;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerInfoSerializer;
import br.com.laboon.core.server.ServerState;
import br.com.laboon.core.server.ServerType;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServerHeartbeat {

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
                        100L,
                        100L
                );
    }

    private void update() {

        String serverName =
                config.getServerName();

        String host =
                "127.0.0.1";

        int port =
                plugin.getServer().getPort();

        int players =
                plugin.getServer()
                        .getOnlinePlayers()
                        .size();

        int maxPlayers =
                100;

        ServerType type =
                ServerType.valueOf(
                        config.getServerType()
                                .toUpperCase()
                );

        ServerInfo server =
                new ServerInfo(
                        serverName,
                        type,
                        host,
                        port,
                        maxPlayers
                );

        server.setPlayers(players);

        server.setState(
                ServerState.ONLINE
        );

        String key =
                "laboon:server:"
                        + serverName;

        redisManager
                .getJedis()
                .hset(
                        key,
                        "name",
                        server.getName()
                );

        redisManager
                .getJedis()
                .hset(
                        key,
                        "type",
                        server.getType().name()
                );

        redisManager
                .getJedis()
                .hset(
                        key,
                        "state",
                        server.getState().name()
                );

        redisManager
                .getJedis()
                .hset(
                        key,
                        "host",
                        server.getHost()
                );

        redisManager
                .getJedis()
                .hset(
                        key,
                        "port",
                        String.valueOf(
                                server.getPort()
                        )
                );

        redisManager
                .getJedis()
                .hset(
                        key,
                        "players",
                        String.valueOf(
                                server.getPlayers()
                        )
                );

        redisManager
                .getJedis()
                .hset(
                        key,
                        "maxPlayers",
                        String.valueOf(
                                server.getMaxPlayers()
                        )
                );

        redisManager
                .getJedis()
                .expire(
                        key,
                        15
                );

        messageBus.publish(
                Channels.SERVER_INFO,
                ServerInfoSerializer.serialize(
                        server
                )
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