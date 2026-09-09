package br.com.laboon.bukkit.server;

import br.com.laboon.bukkit.config.ServerConfig;
import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.redis.RedisManager;
import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerInfoSerializer;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ServerHeartbeat {

    private static final long HEARTBEAT_INTERVAL = 100L;
    private static final int SERVER_TTL = 15;

    private final JavaPlugin plugin;
    private final Supplier<ServerRuntimeState> runtimeState;
    private final RedisManager redisManager;
    private final ServerConfig config;
    private final MessageBus messageBus;

    public ServerHeartbeat(JavaPlugin plugin, Supplier<ServerRuntimeState> runtimeState, RedisManager redisManager, ServerConfig config, MessageBus messageBus) {
        this.plugin = plugin;
        this.runtimeState = runtimeState;
        this.redisManager = redisManager;
        this.config = config;
        this.messageBus = messageBus;
    }

    public void start() {

        update();

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::update, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);
    }

    private void update() {

        ServerInfo server = createServerInfo();

        String key = "laboon:server:" + server.getName();

        Map<String, String> data = new HashMap<>();

        data.put("name", server.getName());

        data.put("type", server.getType().name());

        data.put("role", server.getRole() == null ? "" : server.getRole().name());

        data.put("state", server.getState() == null ? "" : server.getState().name());

        data.put("mode", server.getMode() == null ? "" : server.getMode().name());

        data.put("map", server.getMap() == null ? "" : server.getMap());

        data.put("host", server.getHost());

        data.put("port", String.valueOf(server.getPort()));

        data.put("players", String.valueOf(server.getPlayers()));

        data.put("maxPlayers", String.valueOf(server.getMaxPlayers()));

        redisManager.getJedis().hset(key, data);

        redisManager.getJedis().expire(key, SERVER_TTL);

        messageBus.publish(Channels.SERVER_INFO, ServerInfoSerializer.serialize(server));
    }

    private ServerInfo createServerInfo() {

        ServerInfo server = new ServerInfo(config.getServerName(), config.getServerType(), config.getServerRole(), config.getHost(), config.getPort(), config.getMaxPlayers());

        server.setState(runtimeState.get().getState());

        server.setMode(runtimeState.get().getMode());

        server.setMap(runtimeState.get().getMap());

        server.setPlayers(plugin.getServer().getOnlinePlayers().size());

        return server;
    }

    public void stop() {

        redisManager.getJedis().del("laboon:server:" + config.getServerName());
    }
}