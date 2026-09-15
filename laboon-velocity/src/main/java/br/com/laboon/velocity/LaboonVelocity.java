package br.com.laboon.velocity;

import br.com.laboon.core.account.*;
import br.com.laboon.core.account.cache.AccountCache;
import br.com.laboon.core.account.group.GroupUpdatePublisher;
import br.com.laboon.core.account.punishment.PunishmentService;
import br.com.laboon.core.account.repository.AccountRepository;
import br.com.laboon.core.account.repository.PostgreSqlAccountPreferencesRepository;
import br.com.laboon.core.account.repository.PostgreSqlAccountRepository;
import br.com.laboon.core.account.repository.PostgreSqlPunishmentRepository;
import br.com.laboon.core.account.repository.PostgreSqlTemporaryGroupRepository;
import br.com.laboon.core.command.CommandLoader;
import br.com.laboon.core.command.CommandScanner;
import br.com.laboon.core.database.DatabaseConfig;
import br.com.laboon.core.database.DatabaseManager;
import br.com.laboon.core.database.DatabaseMigrationService;
import br.com.laboon.core.economy.EconomyRepository;
import br.com.laboon.core.economy.EconomyService;
import br.com.laboon.core.economy.PostgreSqlEconomyRepository;
import br.com.laboon.core.friend.FriendManager;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.RedisPublisher;
import br.com.laboon.core.messaging.RedisSubscriber;
import br.com.laboon.core.party.PartyManager;
import br.com.laboon.core.profile.GameCoinsRepository;
import br.com.laboon.core.profile.PlayerProfile;
import br.com.laboon.core.profile.ProfileManager;
import br.com.laboon.core.profile.StatisticsRepository;
import br.com.laboon.core.redis.RedisManager;
import br.com.laboon.core.report.ReportExpirationService;
import br.com.laboon.core.report.ReportManager;
import br.com.laboon.core.rewards.PostgreSqlRewardTransactionRepository;
import br.com.laboon.core.rewards.RewardService;
import br.com.laboon.core.rewards.RewardTransactionRepository;
import br.com.laboon.core.server.ServerRegistry;
import br.com.laboon.core.vanish.GlobalVanishRepository;

import br.com.laboon.velocity.account.VelocityAccountService;
import br.com.laboon.velocity.auth.AuthenticationService;
import br.com.laboon.velocity.auth.MojangProfileService;
import br.com.laboon.velocity.command.VelocityCommandFramework;
import br.com.laboon.velocity.command.VelocityCommandProvider;
import br.com.laboon.velocity.command.commands.LaboonCommand;
import br.com.laboon.velocity.command.commands.LanguageCommand;
import br.com.laboon.velocity.command.commands.ServerCommand;
import br.com.laboon.velocity.config.VelocityConfig;
import br.com.laboon.velocity.language.VelocityLanguage;
import br.com.laboon.velocity.listener.AccountConnectionListener;
import br.com.laboon.velocity.listener.ConnectionListener;
import br.com.laboon.velocity.listener.PunishmentConnectionListener;
import br.com.laboon.velocity.listener.ServerDisconnectListener;
import br.com.laboon.velocity.listener.ServerListener;
import br.com.laboon.velocity.messaging.VelocityMessageService;
import br.com.laboon.velocity.messaging.VelocityPlayerActionService;
import br.com.laboon.velocity.party.PartyServerService;
import br.com.laboon.velocity.player.PlayerManager;
import br.com.laboon.velocity.player.PlayerServerService;
import br.com.laboon.velocity.report.ReportNotificationService;
import br.com.laboon.velocity.server.ProxyHeartbeat;
import br.com.laboon.velocity.server.ProxyServerManager;
import br.com.laboon.velocity.server.ServerAvailabilityService;
import br.com.laboon.velocity.server.ServerCache;
import br.com.laboon.velocity.server.ServerConnectionService;
import br.com.laboon.velocity.server.ServerFallbackService;
import br.com.laboon.velocity.server.ServerRegistrationService;
import br.com.laboon.velocity.server.ServerRegistrySync;
import br.com.laboon.velocity.server.ServerSelector;
import br.com.laboon.velocity.vanish.VelocityVanishService;

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
    private DatabaseManager databaseManager;
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
    private ScheduledTask networkPlayerCountTask;

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
    private AccountCache accountCache;

    private PostgreSqlAccountPreferencesRepository accountPreferencesRepository;
    private PostgreSqlTemporaryGroupRepository temporaryGroupRepository;
    private PostgreSqlPunishmentRepository punishmentRepository;

    private AccountSessionManager accountSessionManager;
    private MojangProfileService mojangProfileService;
    private AuthenticationService authenticationService;
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
    private PartyServerService partyServerService;

    /*
     * =========================
     * AMIGOS
     * =========================
     */

    private FriendManager friendManager;

    /*
     * =============================
     * REPORTS
     * =============================
     */

    private ReportManager reportManager;
    private ReportNotificationService reportNotificationService;
    private ReportExpirationService reportExpirationService;

    private ScheduledTask reportExpirationTask;

    /*
     * =============================
     * PUNISHMENT
     * =============================
     */

    private PunishmentService punishmentService;

    /*
     * =========================
     * VANISH / PLAYER ACTION
     * =========================
     */

    private VelocityPlayerActionService velocityPlayerActionService;
    private GlobalVanishRepository globalVanishRepository;
    private VelocityVanishService velocityVanishService;

    /*
     * =========================
     * ECONOMY / REWARDS
     * =========================
     */

    private EconomyRepository economyRepository;
    private EconomyService economyService;
    private RewardTransactionRepository rewardTransactionRepository;
    private RewardService rewardService;

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

        logger.info("=================================");
        logger.info("          LABOON NETWORK");
        logger.info("=================================");

        VelocityConfig config =
                VelocityConfig.defaultConfig();

        /*
         * =========================
         * INFRAESTRUTURA
         * =========================
         */

        connectRedis(config);
        connectDatabase(config);
        setupLanguage();

        /*
         * =========================
         * DEPENDÊNCIAS
         * =========================
         */

        setupManagers();

        /*
         * =========================
         * MESSAGING
         * =========================
         */

        setupMessaging();

        /*
         * =========================
         * SERVIDORES
         * =========================
         */

        registerServers();
        startServerSync();

        /*
         * =========================
         * REPORTS
         * =========================
         */

        startReportExpiration();

        /*
         * =========================
         * OUTROS SISTEMAS
         * =========================
         */

        setupFriends();
        setupCommandFramework();
        setupHeartbeat(config);

        registerCommands();
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
     * POSTGRESQL
     * =========================
     */

    private void connectDatabase(
            VelocityConfig config
    ) {

        logger.info(
                "Conectando ao PostgreSQL..."
        );

        DatabaseConfig databaseConfig =
                new DatabaseConfig(
                        config.getDatabaseHost(),
                        config.getDatabasePort(),
                        config.getDatabaseName(),
                        config.getDatabaseUsername(),
                        config.getDatabasePassword(),
                        config.getDatabaseMaximumPoolSize(),
                        config.getDatabaseMinimumIdle(),
                        config.getDatabaseConnectionTimeout(),
                        config.getDatabaseIdleTimeout(),
                        config.getDatabaseMaxLifetime()
                );

        logger.info(
                "Banco PostgreSQL configurado: "
                        + databaseConfig.getHost()
                        + ":"
                        + databaseConfig.getPort()
                        + "/"
                        + databaseConfig.getDatabase()
        );

        databaseManager =
                new DatabaseManager(
                        databaseConfig
                );

        DatabaseMigrationService migrationService =
                new DatabaseMigrationService(
                        databaseManager
                );

        migrationService.execute(
                "database/economy.sql"
        );

        migrationService.execute(
                "database/rewards.sql"
        );

        if (!databaseManager.isConnected()) {

            throw new IllegalStateException(
                    "Não foi possível conectar ao PostgreSQL."
            );
        }

        logger.info(
                "PostgreSQL conectado com sucesso!"
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

        temporaryGroupService =
                new TemporaryGroupService(
                        accountRepository
                );

        accountSessionManager =
                new AccountSessionManager();

        mojangProfileService =
                new MojangProfileService();

        authenticationService =
                new AuthenticationService(
                        mojangProfileService
                );

        velocityAccountService =
                new VelocityAccountService(
                        accountManager,
                        accountSessionManager
                );

        /*
         * =========================
         * PUNISHMENT
         * =========================
         */

        punishmentService =
                new PunishmentService(
                        accountManager,
                        punishmentRepository
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

        /*
         * =========================
         * PARTY
         * =========================
         */

        partyManager =
                new PartyManager(
                        redisManager
                );

        partyServerService =
                new PartyServerService(
                        proxyServer,
                        partyManager,
                        serverRegistry
                );

        /*
         * =========================
         * REPORTS
         * =========================
         */

        reportManager =
                new ReportManager(
                        redisManager
                );

        reportNotificationService =
                new ReportNotificationService(
                        proxyServer,
                        accountManager
                );

        reportExpirationService =
                new ReportExpirationService(
                        reportManager
                );

        /*
         * =========================
         * ECONOMY
         * =========================
         */

        economyRepository =
                new PostgreSqlEconomyRepository(
                        databaseManager
                );

        economyService =
                new EconomyService(
                        economyRepository
                );

        /*
         * =========================
         * REWARDS
         * =========================
         */

        rewardTransactionRepository =
                new PostgreSqlRewardTransactionRepository(
                        databaseManager
                );

        rewardService =
                new RewardService(
                        databaseManager,
                        accountManager,
                        rewardTransactionRepository
                );

        logger.info(
                "Economy e Rewards inicializados."
        );

        logger.info(
                "Managers inicializados."
        );
    }

    /*
     * =========================
     * REPORT EXPIRATION
     * =========================
     */

    private void startReportExpiration() {

        reportExpirationTask =
                proxyServer
                        .getScheduler()
                        .buildTask(
                                this,
                                () -> {

                                    if (reportExpirationService == null) {
                                        return;
                                    }

                                    reportExpirationService
                                            .checkExpiredReports();
                                }
                        )
                        .repeat(
                                10,
                                TimeUnit.MINUTES
                        )
                        .schedule();

        logger.info(
                "Expiração de reports iniciada."
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
         * NETWORK
         * =========================
         */

        startNetworkPlayerCount();

        /*
         * =========================
         * GROUP UPDATE
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

        /*
         * =========================
         * ACCOUNT DELIVERY
         * =========================
         */

        AccountDeliveryListener accountDeliveryListener =
                new AccountDeliveryListener(
                        logger,
                        accountRepository,
                        temporaryGroupService,
                        groupUpdatePublisher
                );

        accountDeliveryListener.register(
                messageBus
        );

        /*
         * =========================
         * PLAYER ACTION
         * =========================
         */

        velocityPlayerActionService =
                new VelocityPlayerActionService(
                        proxyServer,
                        messageBus,
                        playerManager,
                        accountManager,
                        serverRegistry,
                        connectionService
                );

        velocityPlayerActionService.register();

        /*
         * =========================
         * GLOBAL VANISH
         * =========================
         */

        globalVanishRepository =
                new GlobalVanishRepository(
                        redisManager
                );

        velocityVanishService =
                new VelocityVanishService(
                        messageBus,
                        globalVanishRepository
                );

        velocityVanishService.start();

        logger.info(
                "PlayerAction + Global Vanish inicializados."
        );

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
                        redisManager
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
                        serverSelector,
                        connectionService,
                        partyManager,
                        partyServerService,
                        friendManager,
                        reportManager,
                        reportNotificationService,
                        punishmentService
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
                                () ->
                                        serverRegistrySync.sync()
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

    private void startNetworkPlayerCount() {

        messageBus.publish(
                Channels.NETWORK,
                String.valueOf(
                        proxyServer.getPlayerCount()
                )
        );

        networkPlayerCountTask =
                proxyServer
                        .getScheduler()
                        .buildTask(
                                this,
                                () ->
                                        messageBus.publish(
                                                Channels.NETWORK,
                                                String.valueOf(
                                                        proxyServer.getPlayerCount()
                                                )
                                        )
                        )
                        .repeat(
                                5,
                                TimeUnit.SECONDS
                        )
                        .schedule();

        logger.info(
                "Contador de jogadores da rede iniciado."
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
                                .aliases(
                                        "servers"
                                )
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
                                profileManager,
                                authenticationService
                        )
                );

        proxyServer
                .getEventManager()
                .register(
                        this,
                        new PunishmentConnectionListener(
                                accountManager
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
            serverSyncTask = null;
        }

        if (networkPlayerCountTask != null) {

            networkPlayerCountTask.cancel();
            networkPlayerCountTask = null;
        }

        if (reportExpirationTask != null) {

            reportExpirationTask.cancel();
            reportExpirationTask = null;
        }

        if (proxyHeartbeat != null) {

            proxyHeartbeat.stop();
            proxyHeartbeat = null;
        }

        /*
         * =========================
         * POSTGRESQL
         * =========================
         */

        if (databaseManager != null) {

            try {

                logger.info(
                        "Fechando PostgreSQL..."
                );

                databaseManager.close();

                logger.info(
                        "PostgreSQL fechado."
                );

            } catch (Exception exception) {

                logger.error(
                        "Erro ao fechar PostgreSQL.",
                        exception
                );
            }

            databaseManager = null;
        }

        /*
         * =========================
         * REDIS
         * =========================
         */

        if (redisManager != null) {

            try {

                logger.info(
                        "Fechando Redis..."
                );

                redisManager.close();

                logger.info(
                        "Redis fechado."
                );

            } catch (Exception exception) {

                logger.error(
                        "Erro ao fechar Redis.",
                        exception
                );
            }

            redisManager = null;
        }

        /*
         * =========================
         * LIMPEZA
         * =========================
         */

        accountService = null;
        accountManager = null;
        accountRepository = null;
        accountCache = null;

        accountPreferencesRepository = null;
        temporaryGroupRepository = null;
        punishmentRepository = null;

        messageBus = null;

        velocityPlayerActionService = null;
        velocityVanishService = null;
        globalVanishRepository = null;

        economyRepository = null;
        economyService = null;
        rewardTransactionRepository = null;
        rewardService = null;

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

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }

    public AccountCache getAccountCache() {
        return accountCache;
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

    public EconomyService getEconomyService() {
        return economyService;
    }

    public RewardTransactionRepository
    getRewardTransactionRepository() {

        return rewardTransactionRepository;
    }

    public RewardService getRewardService() {
        return rewardService;
    }
}