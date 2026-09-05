package br.com.laboon.velocity;

import br.com.laboon.core.account.*;
import br.com.laboon.core.account.group.GroupUpdatePublisher;
import br.com.laboon.core.command.CommandLoader;
import br.com.laboon.core.command.CommandScanner;
import br.com.laboon.core.friend.FriendManager;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.RedisPublisher;
import br.com.laboon.core.messaging.RedisSubscriber;
import br.com.laboon.core.party.PartyManager;
import br.com.laboon.core.profile.GameCoinsRepository;
import br.com.laboon.core.profile.PlayerProfile;
import br.com.laboon.core.profile.ProfileManager;
import br.com.laboon.core.profile.StatisticsRepository;
import br.com.laboon.core.redis.RedisManager;
import br.com.laboon.core.server.ServerRegistry;
import br.com.laboon.velocity.account.VelocityAccountService;
import br.com.laboon.velocity.command.VelocityCommandFramework;
import br.com.laboon.velocity.command.VelocityCommandProvider;
import br.com.laboon.velocity.command.commands.LaboonCommand;
import br.com.laboon.velocity.command.commands.LanguageCommand;
import br.com.laboon.velocity.command.commands.ServerCommand;
import br.com.laboon.velocity.config.VelocityConfig;
import br.com.laboon.velocity.language.VelocityLanguage;
import br.com.laboon.velocity.listener.AccountConnectionListener;
import br.com.laboon.velocity.listener.ConnectionListener;
import br.com.laboon.velocity.listener.ServerDisconnectListener;
import br.com.laboon.velocity.listener.ServerListener;
import br.com.laboon.velocity.messaging.VelocityMessageService;
import br.com.laboon.velocity.party.PartyServerService;
import br.com.laboon.velocity.player.PlayerManager;
import br.com.laboon.velocity.player.PlayerServerService;
import br.com.laboon.velocity.server.*;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "laboon",
        name = "Laboon",
        version = "1.0.0-SNAPSHOT",
        description = "Laboon Network",
        authors = {"oAleDr"}
)
public final class LaboonVelocity {

    private final ProxyServer proxyServer;
    private final Logger logger;

    /*
     * =========================
     * INFRAESTRUTURA
     * =========================
     */

    private RedisManager redisManager;

    private LanguageService languageService;

    private MessageBus messageBus;

    /*
     * =========================
     * SERVIDORES
     * =========================
     */

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

    private ServerCache serverCache;

    private ScheduledTask serverSyncTask;

    /*
     * =========================
     * MENSAGENS
     * =========================
     */

    private VelocityMessageService messageService;

    private GroupUpdatePublisher groupUpdatePublisher;

    /*
     * =========================
     * CONTAS
     * =========================
     */

    private AccountService accountService;

    private AccountManager accountManager;

    private AccountRepository accountRepository;

    private AccountSessionManager accountSessionManager;

    private TemporaryGroupService temporaryGroupService;

    private VelocityAccountService velocityAccountService;

    /*
     * =========================
     * PERFIL
     * =========================
     */

    private StatisticsRepository statisticsRepository;

    private GameCoinsRepository gameCoinsRepository;

    private ProfileManager profileManager;

    /*
     * =========================
     * COMANDOS
     * =========================
     */

    private VelocityCommandProvider commandProvider;

    private VelocityCommandFramework commandFramework;

    /*
     * =========================
     * PARTY
     * =========================
     */

    private PartyManager partyManager;
    private PartyServerService  partyServerService;

    /*
     * =========================
     * AMIGOS
     * =========================
     */

    private FriendManager friendManager;

    @Inject
    public LaboonVelocity(
            ProxyServer proxyServer,
            Logger logger
    ) {

        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    /*
     * =========================
     * INITIALIZE
     * =========================
     */

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

        /*
         * =========================
         * INFRAESTRUTURA
         * =========================
         */

        connectRedis(config);

        setupLanguage();

        /*
         * =========================
         * MANAGERS
         * =========================
         */

        setupManagers();

        /*
         * =========================
         * SERVIDORES
         * =========================
         */

        registerServers();

        startServerSync();

        /*
         * =========================
         * MESSAGING
         * =========================
         */

        setupMessaging();

        /*
         * =========================
         * AMIGOS
         * =========================
         */

        setupFriends();

        /*
         * =========================
         * COMMAND FRAMEWORK
         * =========================
         */

        setupCommandFramework();

        /*
         * =========================
         * HEARTBEAT
         * =========================
         */

        setupHeartbeat(config);

        /*
         * =========================
         * COMANDOS
         * =========================
         */

        registerCommands();

        /*
         * =========================
         * LISTENERS
         * =========================
         */

        registerListeners();

        logger.info(
                "Laboon inicializado com sucesso!"
        );
    }

    /*
     * =========================
     * REDIS
     * =========================
     */

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

    /*
     * =========================
     * LANGUAGE
     * =========================
     */

    private void setupLanguage() {

        Path languageDirectory =
                Path.of(
                        "plugins",
                        "Laboon",
                        "languages"
                );

        VelocityLanguage language =
                new VelocityLanguage(
                        languageDirectory
                );

        languageService =
                language.getService();

        logger.info(
                "Sistema de linguagem inicializado."
        );
    }

    /*
     * =========================
     * MANAGERS
     * =========================
     */

    private void setupManagers() {

        playerManager =
                new PlayerManager(
                        proxyServer
                );

        /*
         * =========================
         * SERVIDORES
         * =========================
         */

        serverRegistry =
                new ServerRegistry(
                        redisManager
                );

        serverManager =
                new ProxyServerManager(
                        serverRegistry
                );

        registrationService =
                new ServerRegistrationService(
                        proxyServer,
                        serverRegistry
                );

        serverCache =
                new ServerCache();

        serverAvailabilityService =
                new ServerAvailabilityService();

        serverRegistrySync =
                new ServerRegistrySync(
                        serverRegistry,
                        registrationService,
                        serverCache
                );

        serverSelector =
                new ServerSelector(
                        serverManager,
                        serverAvailabilityService
                );

        connectionService =
                new ServerConnectionService(
                        proxyServer,
                        serverAvailabilityService
                );

        playerServerService =
                new PlayerServerService(
                        serverRegistry
                );

        fallbackService =
                new ServerFallbackService(
                        serverSelector,
                        connectionService
                );

        /*
         * =========================
         * CONTAS
         * =========================
         */

        accountService =
                new AccountService(
                        redisManager
                );

        accountManager =
                new AccountManager(
                        accountService
                );

        accountRepository =
                new AccountRepository(
                        redisManager
                );

        temporaryGroupService =
                new TemporaryGroupService(
                        accountRepository
                );

        accountSessionManager =
                new AccountSessionManager();

        velocityAccountService =
                new VelocityAccountService(
                        accountManager,
                        accountSessionManager
                );

        /*
         * =========================
         * PERFIL
         * =========================
         */

        statisticsRepository =
                new StatisticsRepository(
                        redisManager
                );

        gameCoinsRepository =
                new GameCoinsRepository(
                        redisManager
                );

        profileManager =
                new ProfileManager(
                        accountManager,
                        statisticsRepository,
                        gameCoinsRepository
                );

        partyManager = new PartyManager(redisManager);
        partyServerService = new PartyServerService(proxyServer, partyManager, serverRegistry);

        logger.info(
                "Managers inicializados."
        );
    }

    /*
     * =========================
     * MESSAGING
     * =========================
     */

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

        /*
         * =========================
         * GRUPO
         * =========================
         */

        groupUpdatePublisher =
                new GroupUpdatePublisher(
                        messageBus
                );

        /*
         * =========================
         * MESSAGE SERVICE
         * =========================
         */

        messageService =
                new VelocityMessageService(
                        messageBus,
                        registrationService
                );

        messageService.listen();

        logger.info(
                "Messaging inicializado."
        );
    }


    /*
     * =========================
     * AMIGOS
     * =========================
     */

    private void setupFriends() {

        friendManager =
                new FriendManager(
                        redisManager,
                        accountManager
                );

        logger.info(
                "Sistema de amigos inicializado."
        );
    }

    /*
     * =========================
     * COMMAND FRAMEWORK
     * =========================
     */

    private void setupCommandFramework() {

        commandProvider =
                new VelocityCommandProvider(
                        proxyServer,
                        profileManager,
                        accountManager,
                        languageService,
                        temporaryGroupService,
                        groupUpdatePublisher,
                        partyManager,
                        partyServerService,
                        friendManager
                );

        commandFramework =
                new VelocityCommandFramework(
                        proxyServer,
                        logger,
                        commandProvider,
                        profileManager
                );

        logger.info(
                "Command Framework inicializado."
        );
    }

    /*
     * =========================
     * HEARTBEAT
     * =========================
     */

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

    /*
     * =========================
     * SERVER REGISTRATION
     * =========================
     */

    private void registerServers() {

        logger.info(
                "Registrando servidores do Redis..."
        );

        registrationService.registerAll();

        logger.info(
                "Servidores registrados."
        );
    }

    /*
     * =========================
     * SERVER SYNC
     * =========================
     */

    private void startServerSync() {

        serverSyncTask =
                proxyServer
                        .getScheduler()
                        .buildTask(
                                this,
                                () -> serverRegistrySync.sync()
                        )
                        .repeat(
                                5,
                                TimeUnit.SECONDS
                        )
                        .schedule();

        logger.info(
                "Sincronização de servidores iniciada."
        );
    }

    /*
     * =========================
     * COMMANDS
     * =========================
     */

    private void registerCommands() {

        logger.info(
                "================================="
        );

        logger.info(
                "Registrando comandos..."
        );

        logger.info(
                "================================="
        );

        CommandLoader.load(
                commandFramework,
                CommandScanner.scan(
                        "br.com.laboon.velocity.command.commands"
                ),
                commandProvider
        );

        logger.info(
                "Comandos carregados pelo CommandLoader."
        );

        /*
         * =========================
         * COMANDOS LEGADOS
         * =========================
         */

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
                                serverSelector,
                                connectionService,
                                serverAvailabilityService,
                                languageService,
                                velocityAccountService
                        )
                );

        proxyServer
                .getCommandManager()
                .register(
                        proxyServer
                                .getCommandManager()
                                .metaBuilder("language")
                                .aliases(
                                        "lang",
                                        "idioma"
                                )
                                .build(),
                        new LanguageCommand(
                                accountManager,
                                languageService
                        )
                );

        logger.info(
                "Comandos registrados."
        );
    }

    /*
     * =========================
     * LISTENERS
     * =========================
     */

    private void registerListeners() {

        proxyServer
                .getEventManager()
                .register(
                        this,
                        new ConnectionListener(
                                proxyServer,
                                fallbackService
                        )
                );

        proxyServer
                .getEventManager()
                .register(
                        this,
                        new ServerListener()
                );

        proxyServer
                .getEventManager()
                .register(
                        this,
                        new ServerDisconnectListener(
                                serverRegistry,
                                serverCache,
                                fallbackService
                        )
                );

        proxyServer
                .getEventManager()
                .register(
                        this,
                        new AccountConnectionListener(
                                velocityAccountService,
                                profileManager
                        )
                );

        logger.info(
                "Listeners registrados."
        );
    }

    /*
     * =========================
     * SHUTDOWN
     * =========================
     */

    @Subscribe
    public void onProxyShutdown(
            ProxyShutdownEvent event
    ) {

        logger.info(
                "Desligando Laboon..."
        );

        if (serverSyncTask != null) {
            serverSyncTask.cancel();
        }

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

    /*
     * =========================
     * GETTERS
     * =========================
     */

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

    public VelocityCommandFramework getCommandFramework() {
        return commandFramework;
    }

    public VelocityCommandProvider getCommandProvider() {
        return commandProvider;
    }

    public TemporaryGroupService getTemporaryGroupService() {
        return temporaryGroupService;
    }

    public PlayerProfile getProfile(
            Player player
    ) {

        if (player == null) {
            return null;
        }

        return profileManager.get(
                player.getUniqueId()
        );
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }
}