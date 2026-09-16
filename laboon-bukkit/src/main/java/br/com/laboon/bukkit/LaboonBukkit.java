package br.com.laboon.bukkit;

import br.com.laboon.bukkit.api.CooldownAPI;
import br.com.laboon.bukkit.api.TitleAPI;
import br.com.laboon.bukkit.api.hologram.HologramListener;
import br.com.laboon.bukkit.api.item.ActionItemStack;
import br.com.laboon.bukkit.api.item.ClickItemListener;
import br.com.laboon.bukkit.api.npc.NPCListener;
import br.com.laboon.bukkit.api.skin.SkinService;
import br.com.laboon.bukkit.chat.ChatFormatter;
import br.com.laboon.bukkit.chat.ChatListener;
import br.com.laboon.bukkit.command.BukkitCommandFramework;
import br.com.laboon.bukkit.command.BukkitCommandProvider;
import br.com.laboon.bukkit.config.DatabaseConfigLoader;
import br.com.laboon.bukkit.config.RedisConfig;
import br.com.laboon.bukkit.config.RedisConfigLoader;
import br.com.laboon.bukkit.config.ServerConfig;
import br.com.laboon.bukkit.config.ServerConfigLoader;
import br.com.laboon.bukkit.display.DisplayListener;
import br.com.laboon.bukkit.display.DisplayManager;
import br.com.laboon.bukkit.group.GroupUpdateListener;
import br.com.laboon.bukkit.gui.AnvilGuiManager;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.bukkit.gui.friend.FriendGui;
import br.com.laboon.bukkit.gui.reports.ReportListGui;
import br.com.laboon.bukkit.listener.PunishmentChatListener;
import br.com.laboon.bukkit.messaging.BukkitPlayerActionService;
import br.com.laboon.bukkit.profile.BukkitProfileProvider;
import br.com.laboon.bukkit.profile.ProfileListener;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.bukkit.server.ServerHeartbeat;
import br.com.laboon.bukkit.server.ServerRuntimeState;
import br.com.laboon.bukkit.vanish.VanishListener;
import br.com.laboon.bukkit.vanish.VanishPlayerView;
import br.com.laboon.bukkit.vanish.VanishService;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.AccountService;
import br.com.laboon.core.account.cache.AccountCache;
import br.com.laboon.core.account.repository.AccountRepository;
import br.com.laboon.core.account.repository.PostgreSqlAccountPreferencesRepository;
import br.com.laboon.core.account.repository.PostgreSqlAccountRepository;
import br.com.laboon.core.account.repository.PostgreSqlPunishmentRepository;
import br.com.laboon.core.account.repository.PostgreSqlTemporaryGroupRepository;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.CommandLoader;
import br.com.laboon.core.command.CommandProvider;
import br.com.laboon.core.command.CommandScanner;
import br.com.laboon.core.database.DatabaseConfig;
import br.com.laboon.core.database.DatabaseManager;
import br.com.laboon.core.database.DatabaseMigrationService;
import br.com.laboon.core.economy.EconomyRepository;
import br.com.laboon.core.economy.EconomyService;
import br.com.laboon.core.economy.PostgreSqlEconomyRepository;
import br.com.laboon.core.friend.FriendManager;
import br.com.laboon.core.language.LanguageBootstrap;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageModule;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.RedisPublisher;
import br.com.laboon.core.messaging.RedisSubscriber;
import br.com.laboon.core.profile.GameCoinsRepository;
import br.com.laboon.core.profile.StatisticsRepository;
import br.com.laboon.core.progression.PostgreSqlProgressionRepository;
import br.com.laboon.core.progression.ProgressionRepository;
import br.com.laboon.core.progression.ProgressionService;
import br.com.laboon.core.progression.prestige.PostgreSqlPrestigeRepository;
import br.com.laboon.core.progression.prestige.PrestigeRepository;
import br.com.laboon.core.progression.prestige.PrestigeService;
import br.com.laboon.core.progression.prestige.PrestigeTransactionRepository;
import br.com.laboon.core.redis.RedisManager;
import br.com.laboon.core.report.ReportManager;
import br.com.laboon.core.rewards.PostgreSqlRewardTransactionRepository;
import br.com.laboon.core.rewards.RewardService;
import br.com.laboon.core.rewards.RewardTransactionRepository;
import br.com.laboon.core.vanish.GlobalVanishRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class LaboonBukkit extends JavaPlugin {

    private static LaboonBukkit instance;

    /*
     * =========================
     * DATABASE / CACHE
     * =========================
     */

    private DatabaseManager databaseManager;

    private AccountRepository accountRepository;

    private PostgreSqlAccountPreferencesRepository accountPreferencesRepository;

    private PostgreSqlTemporaryGroupRepository temporaryGroupRepository;

    private PostgreSqlPunishmentRepository punishmentRepository;

    private AccountCache accountCache;

    private AccountManager accountManager;

    /*
     * =========================
     * REDIS
     * =========================
     */

    private RedisManager redisManager;

    private MessageBus messageBus;

    /*
     * =========================
     * SERVER
     * =========================
     */

    private ServerHeartbeat heartbeat;

    private volatile ServerRuntimeState serverRuntimeState;

    private ServerConfig serverConfig;

    /*
     * =========================
     * GUI
     * =========================
     */

    private GuiManager guiManager;

    private AnvilGuiManager anvilGuiManager;

    /*
     * =========================
     * PROFILE
     * =========================
     */

    private StatisticsRepository statisticsRepository;

    private GameCoinsRepository gameCoinsRepository;

    private ProfileProvider profileProvider;

    private ProfileListener profileListener;

    /*
     * =========================
     * DISPLAY
     * =========================
     */

    private DisplayManager displayManager;

    private DisplayListener tabListener;

    /*
     * =========================
     * CHAT
     * =========================
     */

    private ChatFormatter chatFormatter;

    /*
     * =========================
     * LANGUAGE
     * =========================
     */

    private LanguageService languageService;

    /*
     * =========================
     * COMMANDS
     * =========================
     */

    private BukkitCommandFramework commandFramework;

    /*
     * =========================
     * GROUP
     * =========================
     */

    private GroupUpdateListener groupUpdateListener;

    /*
     * =========================
     * REPORT
     * =========================
     */

    private ReportManager reportManager;

    private ReportListGui reportListGui;

    /*
     * =========================
     * FRIEND
     * =========================
     */

    private FriendManager friendManager;

    private FriendGui friendGui;

    /*
     * =========================
     * API
     * =========================
     */

    private HologramListener hologramListener;

    private TitleAPI titleAPI;

    private SkinService skinService;

    /*
     * =========================
     * VANISH
     * =========================
     */

    private BukkitPlayerActionService bukkitPlayerActionService;

    private GlobalVanishRepository globalVanishRepository;

    private VanishService vanishService;

    private VanishPlayerView vanishPlayerView;

    /*
     * =========================
     * ECONOMY / REWARDS
     * =========================
     */

    private EconomyRepository economyRepository;

    private EconomyService economyService;

    private RewardTransactionRepository rewardTransactionRepository;

    private RewardService rewardService;

    /*
     * =========================
     * PROGRESSION
     * =========================
     */

    private ProgressionRepository progressionRepository;

    private ProgressionService progressionService;

    /*
     * =========================
     * PRESTIGE
     * =========================
     */

    private PrestigeRepository prestigeRepository;

    private PrestigeTransactionRepository prestigeTransactionRepository;

    private PrestigeService prestigeService;

    /*
     * =========================
     * ENABLE
     * =========================
     */

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

        /*
         * =========================
         * CONFIG
         * =========================
         */

        saveDefaultConfig();

        /*
         * =========================
         * SERVER
         * =========================
         */

        loadServerConfig();

        /*
         * =========================
         * REDIS
         * =========================
         */

        if (!connectRedis()) {

            disablePlugin(
                    "Redis não está disponível. Plugin será desativado."
            );

            return;
        }

        /*
         * =========================
         * POSTGRESQL
         * =========================
         */

        if (!connectDatabase()) {

            disablePlugin(
                    "PostgreSQL não está disponível. Plugin será desativado."
            );

            return;
        }

        /*
         * =========================
         * REPOSITORIES
         * =========================
         */

        setupRepositories();

        /*
         * =========================
         * LANGUAGE
         * =========================
         */

        setupLanguage();

        /*
         * =========================
         * MESSAGING
         * =========================
         */

        setupMessaging();

        /*
         * =========================
         * LISTENERS
         * =========================
         */

        registerListeners();

        /*
         * =========================
         * COMMANDS
         * =========================
         */

        registerCommands();

        /*
         * =========================
         * BUKKIT APIS
         * =========================
         */

        CooldownAPI.initialize(this);

        startHologramAPI();

        /*
         * =========================
         * HEARTBEAT
         * =========================
         */

        startHeartbeat();

        if (tabListener != null) {
            tabListener.start();
        }

        if (profileListener != null) {
            profileListener.start();
        }

        /*
         * =========================
         * ACTION ITEMS
         * =========================
         */

        ActionItemStack.init(this);

        getLogger().info(
                "Laboon Bukkit iniciado com sucesso!"
        );
    }

    /*
     * =========================
     * SERVER CONFIG
     * =========================
     */

    private void loadServerConfig() {

        serverConfig =
                ServerConfigLoader.load(this);

        getLogger().info(
                "Servidor configurado: "
                        + serverConfig.getServerName()
                        + " ["
                        + serverConfig.getServerType()
                        + "/"
                        + serverConfig.getServerRole()
                        + "]"
        );
    }

    /*
     * =========================
     * REDIS
     * =========================
     */

    private boolean connectRedis() {

        getLogger().info(
                "Conectando ao Redis..."
        );

        try {

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

                return false;
            }

            getLogger().info(
                    "Redis conectado com sucesso!"
            );

            return true;

        } catch (Exception exception) {

            getLogger().severe(
                    "Erro ao inicializar Redis:"
            );

            exception.printStackTrace();

            return false;
        }
    }

    /*
     * =========================
     * DATABASE
     * =========================
     */

    private boolean connectDatabase() {

        getLogger().info(
                "Conectando ao PostgreSQL..."
        );

        try {

            DatabaseConfig config =
                    DatabaseConfigLoader.load(this);

            getLogger().info(
                    "Banco PostgreSQL configurado: "
                            + config.getHost()
                            + ":"
                            + config.getPort()
                            + "/"
                            + config.getDatabase()
            );

            databaseManager =
                    new DatabaseManager(
                            config
                    );

            /*
             * =========================
             * MIGRATIONS
             * =========================
             */

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

            migrationService.execute(
                    "database/progression.sql"
            );

            migrationService.execute(
                    "database/prestige.sql"
            );

            if (!databaseManager.isConnected()) {

                getLogger().severe(
                        "Não foi possível conectar ao PostgreSQL!"
                );

                return false;
            }

            getLogger().info(
                    "PostgreSQL conectado com sucesso!"
            );

            return true;

        } catch (Exception exception) {

            getLogger().severe(
                    "Erro ao inicializar PostgreSQL:"
            );

            exception.printStackTrace();

            if (databaseManager != null) {

                try {

                    databaseManager.close();

                } catch (Exception ignored) {
                }
            }

            databaseManager = null;

            return false;
        }
    }

    /*
     * =========================
     * REPOSITORIES
     * =========================
     */

    private void setupRepositories() {

        /*
         * =========================
         * ACCOUNT
         * =========================
         */

        accountRepository =
                new PostgreSqlAccountRepository(
                        databaseManager
                );

        /*
         * =========================
         * ACCOUNT PREFERENCES
         * =========================
         */

        accountPreferencesRepository =
                new PostgreSqlAccountPreferencesRepository(
                        databaseManager
                );

        /*
         * =========================
         * TEMPORARY GROUPS
         * =========================
         */

        temporaryGroupRepository =
                new PostgreSqlTemporaryGroupRepository(
                        databaseManager
                );

        /*
         * =========================
         * PUNISHMENTS
         * =========================
         */

        punishmentRepository =
                new PostgreSqlPunishmentRepository(
                        databaseManager
                );

        /*
         * =========================
         * REDIS CACHE
         * =========================
         */

        accountCache =
                new AccountCache(
                        redisManager
                );

        /*
         * =========================
         * ACCOUNT SERVICE
         * =========================
         */

        AccountService accountService =
                new AccountService(
                        accountRepository,
                        accountCache,
                        accountPreferencesRepository,
                        temporaryGroupRepository,
                        punishmentRepository
                );

        /*
         * =========================
         * ACCOUNT MANAGER
         * =========================
         */

        accountManager =
                new AccountManager(
                        accountService
                );

        /*
         * =========================
         * STATISTICS
         * =========================
         */

        statisticsRepository =
                new StatisticsRepository(
                        redisManager
                );

        /*
         * =========================
         * GAME COINS
         * =========================
         */

        gameCoinsRepository =
                new GameCoinsRepository(
                        redisManager
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

        /*
         * =========================
         * FRIENDS
         * =========================
         */

        friendManager =
                new FriendManager(
                        redisManager
                );

        /*
         * =========================
         * SKIN
         * =========================
         */

        skinService =
                new SkinService(this);

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

        /*
         * =========================
         * PROGRESSION
         * =========================
         */

        /*
         * Use aqui sua implementação PostgreSQL
         * da ProgressionRepository.
         */
        progressionRepository =
                new PostgreSqlProgressionRepository(
                        databaseManager
                );

        progressionService =
                new ProgressionService(
                        accountManager,
                        progressionRepository
                );

        /*
         * =========================
         * PRESTIGE
         * =========================
         */

        PostgreSqlPrestigeRepository postgresPrestigeRepository =
                new PostgreSqlPrestigeRepository(
                        databaseManager
                );

        prestigeRepository =
                postgresPrestigeRepository;

        prestigeTransactionRepository =
                postgresPrestigeRepository;

        prestigeService =
                new PrestigeService(
                        databaseManager,
                        accountManager,
                        prestigeRepository,
                        prestigeTransactionRepository,
                        progressionService
                );

        getLogger().info(
                "Economy inicializada."
        );

        getLogger().info(
                "Rewards inicializados."
        );

        getLogger().info(
                "Progression inicializada."
        );

        getLogger().info(
                "Prestige inicializado."
        );

        getLogger().info(
                "Repositories e serviços de dados inicializados."
        );
    }

    /*
     * =========================
     * LANGUAGE
     * =========================
     */

    private void setupLanguage() {

        LanguageModule module =
                new LanguageModule(
                        "paper",
                        getDataFolder()
                                .toPath()
                                .resolve("languages")
                );

        languageService =
                LanguageBootstrap.create(
                        LanguageLocale.ptBR(),
                        List.of(module),
                        List.of(
                                LanguageLocale.ptBR(),
                                LanguageLocale.enUS(),
                                LanguageLocale.esES()
                        )
                );

        Path languages =
                getDataFolder()
                        .toPath()
                        .resolve("languages");

        getLogger().info(
                "pt_BR/paper.yml existe: "
                        + Files.exists(
                        languages
                                .resolve("pt_BR")
                                .resolve("paper.yml")
                )
        );

        getLogger().info(
                "en_US/paper.yml existe: "
                        + Files.exists(
                        languages
                                .resolve("en_US")
                                .resolve("paper.yml")
                )
        );

        getLogger().info(
                "es_ES/paper.yml existe: "
                        + Files.exists(
                        languages
                                .resolve("es_ES")
                                .resolve("paper.yml")
                )
        );

        getLogger().info(
                "Sistema de idiomas iniciado."
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
         * PROFILE PROVIDER
         * =========================
         */

        profileProvider =
                new BukkitProfileProvider(
                        accountRepository,
                        statisticsRepository,
                        gameCoinsRepository
                );

        /*
         * =========================
         * DISPLAY
         * =========================
         */

        displayManager =
                new DisplayManager(
                        profileProvider,
                        serverConfig
                );

        /*
         * =========================
         * GROUP UPDATE
         * =========================
         */

        groupUpdateListener =
                new GroupUpdateListener(
                        this,
                        profileProvider,
                        displayManager,
                        messageBus
                );

        groupUpdateListener.register();

        /*
         * =========================
         * VANISH
         * =========================
         */

        bukkitPlayerActionService =
                new BukkitPlayerActionService(
                        this,
                        messageBus
                );

        bukkitPlayerActionService.register();

        globalVanishRepository =
                new GlobalVanishRepository(
                        redisManager
                );

        vanishService =
                new VanishService(
                        this,
                        messageBus,
                        globalVanishRepository,
                        accountManager
                );

        vanishService.register();

        vanishPlayerView =
                new VanishPlayerView(
                        vanishService
                );

        getLogger().info(
                "Messaging inicializado."
        );
    }

    /*
     * =========================
     * LISTENERS
     * =========================
     */

    private void registerListeners() {

        /*
         * GUI
         */

        guiManager =
                new GuiManager();

        anvilGuiManager =
                new AnvilGuiManager(
                        this
                );

        friendGui =
                new FriendGui(
                        guiManager,
                        anvilGuiManager,
                        friendManager,
                        accountManager
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new VanishListener(
                                vanishService
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        guiManager,
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        anvilGuiManager,
                        this
                );

        /*
         * Reports
         */

        reportListGui =
                new ReportListGui(
                        guiManager,
                        reportManager,
                        anvilGuiManager,
                        bukkitPlayerActionService
                );

        /*
         * Profile
         */

        profileListener =
                new ProfileListener(
                        this,
                        profileProvider,
                        accountManager
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        profileListener,
                        this
                );

        /*
         * Display
         */

        tabListener =
                new DisplayListener(
                        this,
                        displayManager
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        tabListener,
                        this
                );

        /*
         * Chat
         */

        chatFormatter =
                new ChatFormatter(
                        profileProvider
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new ChatListener(
                                this,
                                chatFormatter
                        ),
                        this
                );

        /*
         * Punishment
         */

        PunishmentChatListener punishmentChatListener =
                new PunishmentChatListener(
                        profileProvider
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        punishmentChatListener,
                        this
                );

        /*
         * Hologram
         */

        hologramListener =
                new HologramListener(
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        hologramListener,
                        this
                );

        /*
         * NPC
         */

        getServer()
                .getPluginManager()
                .registerEvents(
                        new NPCListener(this),
                        this
                );

        /*
         * Title
         */

        titleAPI =
                new TitleAPI();

        getServer()
                .getPluginManager()
                .registerEvents(
                        titleAPI,
                        this
                );

        /*
         * Item Click
         */

        getServer()
                .getPluginManager()
                .registerEvents(
                        new ClickItemListener(),
                        this
                );

        getLogger().info(
                "Listeners registrados."
        );
    }

    /*
     * =========================
     * HOLOGRAM
     * =========================
     */

    private void startHologramAPI() {

        if (hologramListener == null) {
            return;
        }

        hologramListener.start();

        getLogger().info(
                "Hologram API iniciada."
        );
    }

    /*
     * =========================
     * COMMANDS
     * =========================
     */

    private void registerCommands() {

        commandFramework =
                new BukkitCommandFramework(
                        this,
                        profileProvider
                );

        CommandProvider commandProvider =
                new BukkitCommandProvider(
                        this,
                        skinService,
                        guiManager,
                        anvilGuiManager,
                        profileProvider,
                        languageService,
                        reportListGui,
                        friendGui,
                        accountManager,
                        redisManager
                );

        List<Class<? extends CommandClass>> commands =
                CommandScanner.scan(
                        "br.com.laboon.bukkit.command.commands"
                );

        CommandLoader.load(
                commandFramework,
                commands,
                commandProvider
        );

        getLogger().info(
                "Comandos registrados: "
                        + commands.size()
        );
    }

    /*
     * =========================
     * HEARTBEAT
     * =========================
     */

    private void startHeartbeat() {

        serverRuntimeState =
                new ServerRuntimeState();

        heartbeat =
                new ServerHeartbeat(
                        this,
                        () -> serverRuntimeState,
                        redisManager,
                        serverConfig,
                        messageBus
                );

        heartbeat.start();

        getLogger().info(
                "Heartbeat iniciado: "
                        + serverConfig.getServerName()
                        + " ["
                        + serverConfig.getServerType()
                        + "/"
                        + serverConfig.getServerRole()
                        + "]"
        );
    }

    /*
     * =========================
     * DISABLE
     * =========================
     */

    private void disablePlugin(
            String message
    ) {

        getLogger().severe(
                message
        );

        getServer()
                .getPluginManager()
                .disablePlugin(this);
    }

    /*
     * =========================
     * DISABLE
     * =========================
     */

    @Override
    public void onDisable() {

        getLogger().info(
                "Desligando Laboon..."
        );

        /*
         * =========================
         * HOLOGRAM
         * =========================
         */

        if (hologramListener != null) {

            try {

                hologramListener.stop();

            } catch (Exception exception) {

                getLogger().warning(
                        "Erro ao desligar Hologram API: "
                                + exception.getMessage()
                );
            }
        }

        /*
         * =========================
         * COOLDOWN
         * =========================
         */

        try {

            CooldownAPI.shutdown();

        } catch (Exception exception) {

            getLogger().warning(
                    "Erro ao desligar CooldownAPI: "
                            + exception.getMessage()
            );
        }

        /*
         * =========================
         * DISPLAY
         * =========================
         */

        if (tabListener != null) {

            try {

                tabListener.stop();

            } catch (Exception exception) {

                getLogger().warning(
                        "Erro ao desligar DisplayListener: "
                                + exception.getMessage()
                );
            }
        }

        /*
         * =========================
         * PROFILES
         * =========================
         */

        if (profileProvider != null) {

            try {

                profileProvider.saveAll();

            } catch (Exception exception) {

                getLogger().severe(
                        "Erro ao salvar profiles:"
                );

                exception.printStackTrace();
            }
        }

        /*
         * =========================
         * HEARTBEAT
         * =========================
         */

        if (heartbeat != null) {

            try {

                heartbeat.stop();

            } catch (Exception exception) {

                getLogger().warning(
                        "Erro ao desligar heartbeat: "
                                + exception.getMessage()
                );
            }
        }

        /*
         * =========================
         * PROFILE LISTENER
         * =========================
         */

        if (profileListener != null) {

            try {

                profileListener.stop();

            } catch (Exception exception) {

                getLogger().warning(
                        "Erro ao desligar ProfileListener: "
                                + exception.getMessage()
                );
            }
        }

        /*
         * =========================
         * DISPLAY MANAGER
         * =========================
         */

        if (displayManager != null) {

            try {

                displayManager.shutdown();

            } catch (Exception exception) {

                getLogger().warning(
                        "Erro ao desligar DisplayManager: "
                                + exception.getMessage()
                );
            }
        }

        /*
         * =========================
         * POSTGRESQL
         * =========================
         */

        if (databaseManager != null) {

            try {

                getLogger().info(
                        "Fechando PostgreSQL..."
                );

                databaseManager.close();

                getLogger().info(
                        "PostgreSQL fechado."
                );

            } catch (Exception exception) {

                getLogger().severe(
                        "Erro ao fechar PostgreSQL:"
                );

                exception.printStackTrace();
            }
        }

        /*
         * =========================
         * REDIS
         * =========================
         */

        if (redisManager != null) {

            try {

                getLogger().info(
                        "Fechando Redis..."
                );

                redisManager.close();

                getLogger().info(
                        "Redis fechado."
                );

            } catch (Exception exception) {

                getLogger().severe(
                        "Erro ao fechar Redis:"
                );

                exception.printStackTrace();
            }
        }

        /*
         * =========================
         * CLEAR REFERENCES
         * =========================
         */

        accountManager = null;
        accountRepository = null;
        accountPreferencesRepository = null;
        temporaryGroupRepository = null;
        punishmentRepository = null;
        accountCache = null;

        databaseManager = null;
        redisManager = null;

        economyRepository = null;
        economyService = null;

        rewardTransactionRepository = null;
        rewardService = null;

        progressionRepository = null;
        progressionService = null;

        prestigeRepository = null;
        prestigeTransactionRepository = null;
        prestigeService = null;

        getLogger().info(
                "Laboon Bukkit encerrado."
        );

        instance = null;
    }

    /*
     * =========================
     * GETTERS
     * =========================
     */

    public static LaboonBukkit getInstance() {
        return instance;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }

    public AccountCache getAccountCache() {
        return accountCache;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public AnvilGuiManager getAnvilGuiManager() {
        return anvilGuiManager;
    }

    public LanguageService getLanguageService() {
        return languageService;
    }

    public ProfileProvider getProfileProvider() {
        return profileProvider;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public FriendGui getFriendGui() {
        return friendGui;
    }

    public ServerRuntimeState getServerRuntimeState() {
        return serverRuntimeState;
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public PostgreSqlAccountPreferencesRepository
    getAccountPreferencesRepository() {
        return accountPreferencesRepository;
    }

    public PostgreSqlTemporaryGroupRepository
    getTemporaryGroupRepository() {
        return temporaryGroupRepository;
    }

    public PostgreSqlPunishmentRepository
    getPunishmentRepository() {
        return punishmentRepository;
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

    public ProgressionRepository getProgressionRepository() {
        return progressionRepository;
    }

    public ProgressionService getProgressionService() {
        return progressionService;
    }

    public PrestigeRepository getPrestigeRepository() {
        return prestigeRepository;
    }

    public PrestigeTransactionRepository getPrestigeTransactionRepository() {
        return prestigeTransactionRepository;
    }

    public PrestigeService getPrestigeService() {
        return prestigeService;
    }
}