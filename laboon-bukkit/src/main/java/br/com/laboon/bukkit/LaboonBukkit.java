package br.com.laboon.bukkit;

import br.com.laboon.bukkit.config.RedisConfig;
import br.com.laboon.bukkit.config.RedisConfigLoader;
import br.com.laboon.bukkit.config.ServerConfig;
import br.com.laboon.bukkit.config.ServerConfigLoader;
import br.com.laboon.bukkit.server.ServerHeartbeat;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.RedisPublisher;
import br.com.laboon.core.messaging.RedisSubscriber;
import br.com.laboon.core.redis.RedisManager;

import org.bukkit.plugin.java.JavaPlugin;

public final class LaboonBukkit extends JavaPlugin {

    private static LaboonBukkit instance;

    private RedisManager redisManager;
    private MessageBus messageBus;
    private ServerHeartbeat heartbeat;

    @Override
    public void onEnable() {

        instance = this;

        getLogger().info(
                "================================="
        );

        getLogger().info(
                "          LABOON BUKKIT"
        );

        getLogger().info(
                "================================="
        );

        connectRedis();

        setupMessaging();

        saveDefaultConfig();

        startHeartbeat();

        getLogger().info(
                "Laboon Bukkit iniciado!"
        );
    }

    private void connectRedis() {

        getLogger().info(
                "Conectando ao Redis..."
        );

        RedisConfig config =
                RedisConfigLoader.load(this);

        redisManager =
                new RedisManager(
                        config.getHost(),
                        config.getPort()
                );

        if (!redisManager.isConnected()) {

            getLogger().severe(
                    "Não foi possível conectar ao Redis!"
            );

            getServer()
                    .getPluginManager()
                    .disablePlugin(this);

            return;
        }

        getLogger().info(
                "Redis conectado com sucesso!"
        );
    }

    private void setupMessaging() {

        RedisPublisher publisher =
                new RedisPublisher(
                        redisManager
                );

        RedisSubscriber subscriber =
                new RedisSubscriber(
                        redisManager
                );

        messageBus =
                new MessageBus(
                        publisher,
                        subscriber
                );
    }


    private void startHeartbeat() {

        ServerConfig config =
                ServerConfigLoader.load(this);

        heartbeat =
                new ServerHeartbeat(
                        this,
                        redisManager,
                        config,
                        messageBus
                );

        heartbeat.start();

        getLogger().info(
                "Heartbeat iniciado: "
                        + config.getServerName()
                        + " ["
                        + config.getServerType()
                        + "/"
                        + config.getServerRole()
                        + "]"
        );
    }

    @Override
    public void onDisable() {

        if (heartbeat != null) {
            heartbeat.stop();
        }

        if (redisManager != null) {
            redisManager.close();
        }

        getLogger().info(
                "Laboon Bukkit encerrado."
        );

        instance = null;
    }

    public static LaboonBukkit getInstance() {
        return instance;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }
}