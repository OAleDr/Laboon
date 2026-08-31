package br.com.laboon.velocity;

import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.RedisPublisher;
import br.com.laboon.core.messaging.RedisSubscriber;
import br.com.laboon.core.redis.RedisManager;
import br.com.laboon.core.server.ServerRegistry;

import br.com.laboon.velocity.command.LaboonCommand;
import br.com.laboon.velocity.command.ServerCommand;
import br.com.laboon.velocity.config.VelocityConfig;
import br.com.laboon.velocity.language.VelocityLanguage;
import br.com.laboon.velocity.listener.ConnectionListener;
import br.com.laboon.velocity.listener.ServerDisconnectListener;
import br.com.laboon.velocity.listener.ServerListener;
import br.com.laboon.velocity.messaging.VelocityMessageService;
import br.com.laboon.velocity.player.PlayerManager;
import br.com.laboon.velocity.player.PlayerServerService;
import br.com.laboon.velocity.server.*;

import com.google.inject.Inject;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import com.velocitypowered.api.scheduler.ScheduledTask;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(id = "laboon", name = "Laboon", version = "1.0.0-SNAPSHOT", description = "Laboon Network", authors = {"Laboon"})
public final class LaboonVelocity {

    private final ProxyServer proxyServer;
    private final Logger logger;

    private RedisManager redisManager;

    private LanguageService languageService;

    private MessageBus messageBus;

    private PlayerManager playerManager;

    private ProxyServerManager serverManager;

    private ServerConnectionService connectionService;

    private ServerRegistrationService registrationService;

    private ServerRegistry serverRegistry;

    private ServerRegistrySync serverRegistrySync;

    private ServerSelector serverSelector;

    private ProxyHeartbeat proxyHeartbeat;

    private ServerAvailabilityService serverAvailabilityService;

    private ServerFallbackService fallbackService;

    private PlayerServerService playerServerService;

    private VelocityMessageService messageService;

    private ServerCache serverCache;

    private ScheduledTask serverSyncTask;

    @Inject
    public LaboonVelocity(ProxyServer proxyServer, Logger logger) {

        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {

        logger.info("=================================");

        logger.info("          LABOON NETWORK");

        logger.info("=================================");

        VelocityConfig config = VelocityConfig.defaultConfig();

        connectRedis(config);

        setupLanguage();

        setupManagers();

        registerServers();

        startServerSync();

        setupMessaging();

        setupHeartbeat(config);

        registerCommands();

        registerListeners();


        logger.info("Laboon inicializado com sucesso!");
    }

    private void connectRedis(VelocityConfig config) {

        logger.info("Conectando ao Redis...");

        redisManager = new RedisManager(config.getRedisHost(), config.getRedisPort());

        if (!redisManager.isConnected()) {

            throw new IllegalStateException("Não foi possível conectar ao Redis.");
        }

        logger.info("Redis conectado com sucesso!");
    }

    private void setupLanguage() {

        Path languageDirectory = Path.of("plugins", "Laboon", "languages");

        VelocityLanguage language = new VelocityLanguage(languageDirectory);

        languageService = language.getService();

        logger.info("Sistema de linguagem inicializado.");
    }

    private void setupManagers() {

        playerManager = new PlayerManager(proxyServer);
        serverRegistry = new ServerRegistry(redisManager);
        serverManager = new ProxyServerManager(serverRegistry);
        registrationService = new ServerRegistrationService(proxyServer, serverRegistry);
        serverCache = new ServerCache();
        serverAvailabilityService = new ServerAvailabilityService();
        serverRegistrySync = new ServerRegistrySync(serverRegistry, registrationService, serverCache);
        serverSelector = new ServerSelector(serverManager, serverAvailabilityService);
        connectionService = new ServerConnectionService(proxyServer, serverAvailabilityService);
        playerServerService = new PlayerServerService(serverRegistry);

        fallbackService = new ServerFallbackService(serverSelector, connectionService);
    }

    private void setupMessaging() {

        RedisPublisher publisher = new RedisPublisher(redisManager);

        RedisSubscriber subscriber = new RedisSubscriber(redisManager);

        messageBus = new MessageBus(publisher, subscriber);

        messageService = new VelocityMessageService(messageBus, registrationService);

        messageService.listen();

        logger.info("Messaging inicializado.");
    }

    private void setupHeartbeat(VelocityConfig config) {

        proxyHeartbeat = new ProxyHeartbeat(redisManager, config.getProxyName());

        proxyHeartbeat.start();

        logger.info("Proxy Heartbeat iniciado.");
    }

    private void registerServers() {

        logger.info("Registrando servidores do Redis...");

        registrationService.registerAll();

        logger.info("Servidores registrados.");
    }

    private void startServerSync() {

        serverSyncTask = proxyServer.getScheduler().buildTask(this, () -> serverRegistrySync.sync()).repeat(5, TimeUnit.SECONDS).schedule();

        logger.info("Sincronização de servidores iniciada.");
    }

    private void registerCommands() {

        proxyServer.getCommandManager().register(proxyServer.getCommandManager().metaBuilder("laboon").build(),

                new LaboonCommand());

        proxyServer.getCommandManager().register(proxyServer.getCommandManager().metaBuilder("server").aliases("servers").build(),

                new ServerCommand(serverSelector, connectionService, serverAvailabilityService, languageService));

        logger.info("Comandos registrados.");
    }

    private void registerListeners() {

        proxyServer.getEventManager().register(this, new ConnectionListener(proxyServer, fallbackService));

        proxyServer.getEventManager().register(this, new ServerListener());

        proxyServer.getEventManager().register(this, new ServerDisconnectListener(serverRegistry, serverCache, fallbackService));

        logger.info("Listeners registrados.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {

        logger.info("Desligando Laboon...");

        if (serverSyncTask != null) {
            serverSyncTask.cancel();
        }

        if (proxyHeartbeat != null) {
            proxyHeartbeat.stop();
        }

        if (redisManager != null) {
            redisManager.close();
        }

        logger.info("Laboon encerrado.");
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

    public ServerAvailabilityService getServerAvailabilityService() {
        return serverAvailabilityService;
    }

    public ServerFallbackService getFallbackService() {
        return fallbackService;
    }

    public PlayerServerService getPlayerServerService() {
        return playerServerService;
    }

    public ServerCache getServerCache() {
        return serverCache;
    }

    public LanguageService getLanguageService() {
        return languageService;
    }

}