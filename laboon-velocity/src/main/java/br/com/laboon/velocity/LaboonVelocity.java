package br.com.laboon.velocity;

import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.RedisPublisher;
import br.com.laboon.core.messaging.RedisSubscriber;
import br.com.laboon.core.redis.RedisManager;
import br.com.laboon.core.server.ServerRegistry;

import br.com.laboon.velocity.command.LaboonCommand;
import br.com.laboon.velocity.command.ServerCommand;
import br.com.laboon.velocity.config.VelocityConfig;
import br.com.laboon.velocity.listener.ConnectionListener;
import br.com.laboon.velocity.listener.ServerListener;
import br.com.laboon.velocity.messaging.VelocityMessageService;
import br.com.laboon.velocity.player.PlayerManager;
import br.com.laboon.velocity.server.ProxyHeartbeat;
import br.com.laboon.velocity.server.ProxyServerManager;
import br.com.laboon.velocity.server.ServerConnectionService;
import br.com.laboon.velocity.server.ServerSelector;

import com.google.inject.Inject;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

@Plugin(
        id = "laboon",
        name = "Laboon",
        version = "1.0.0-SNAPSHOT",
        description = "Laboon Network",
        authors = {"Laboon"}
)
public final class LaboonVelocity {

    private final ProxyServer proxyServer;
    private final Logger logger;

    private RedisManager redisManager;

    private MessageBus messageBus;

    private PlayerManager playerManager;

    private ProxyServerManager serverManager;

    private ServerConnectionService connectionService;

    private ServerSelector serverSelector;

    private ProxyHeartbeat proxyHeartbeat;

    private VelocityMessageService messageService;

    @Inject
    public LaboonVelocity(
            ProxyServer proxyServer,
            Logger logger
    ) {

        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(
            ProxyInitializeEvent event
    ) {

        logger.info(
                "================================="
        );

        logger.info(
                "          LABOON NETWORK"
        );

        logger.info(
                "================================="
        );

        VelocityConfig config =
                VelocityConfig.defaultConfig();

        connectRedis(config);

        setupManagers();

        setupMessaging();

        setupHeartbeat(config);

        registerCommands();

        registerListeners();

        logger.info(
                "Laboon inicializado com sucesso!"
        );
    }

    private void connectRedis(
            VelocityConfig config
    ) {

        logger.info(
                "Conectando ao Redis..."
        );

        redisManager =
                new RedisManager(
                        config.getRedisHost(),
                        config.getRedisPort()
                );

        if (!redisManager.isConnected()) {

            throw new IllegalStateException(
                    "Não foi possível conectar ao Redis."
            );
        }

        logger.info(
                "Redis conectado com sucesso!"
        );
    }

    private void setupManagers() {

        playerManager =
                new PlayerManager(
                        proxyServer
                );

        ServerRegistry registry =
                new ServerRegistry(
                        redisManager
                );

        serverManager = new ProxyServerManager(registry);
        serverSelector = new ServerSelector(serverManager);
        connectionService = new ServerConnectionService(proxyServer);
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

        messageService =
                new VelocityMessageService(
                        messageBus
                );

        messageService.listen();

        logger.info(
                "Messaging inicializado."
        );
    }

    private void setupHeartbeat(
            VelocityConfig config
    ) {

        proxyHeartbeat =
                new ProxyHeartbeat(
                        redisManager,
                        config.getProxyName()
                );

        proxyHeartbeat.start();

        logger.info(
                "Proxy Heartbeat iniciado."
        );
    }

    private void registerCommands() {

        proxyServer
                .getCommandManager()
                .register(
                        proxyServer
                                .getCommandManager()
                                .metaBuilder("laboon")
                                .build(),

                        new LaboonCommand()
                );

        proxyServer
                .getCommandManager()
                .register(
                        proxyServer
                                .getCommandManager()
                                .metaBuilder("server")
                                .aliases("servers")
                                .build(),

                        new ServerCommand(
                                playerManager,
                                serverSelector,
                                connectionService
                        )
                );

        logger.info(
                "Comandos registrados."
        );
    }

    private void registerListeners() {

        proxyServer
                .getEventManager()
                .register(
                        this,
                        new ConnectionListener(
                                serverSelector,
                                connectionService
                        )
                );

        proxyServer
                .getEventManager()
                .register(
                        this,
                        new ServerListener()
                );

        logger.info(
                "Listeners registrados."
        );
    }

    @Subscribe
    public void onProxyShutdown(
            ProxyShutdownEvent event
    ) {

        logger.info(
                "Desligando Laboon..."
        );

        if (proxyHeartbeat != null) {
            proxyHeartbeat.stop();
        }

        if (redisManager != null) {
            redisManager.close();
        }

        logger.info(
                "Laboon encerrado."
        );
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ProxyServerManager getServerManager() {
        return serverManager;
    }

    public ServerSelector getServerSelector() {
        return serverSelector;
    }
}