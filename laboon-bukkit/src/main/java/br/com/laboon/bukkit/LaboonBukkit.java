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
import br.com.laboon.bukkit.profile.BukkitProfileProvider;
import br.com.laboon.bukkit.profile.ProfileListener;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.bukkit.server.ServerHeartbeat;
import br.com.laboon.bukkit.server.ServerRuntimeState;

import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.AccountService;
import br.com.laboon.core.account.cache.AccountCache;
import br.com.laboon.core.account.repository.AccountRepository;
import br.com.laboon.core.account.repository.PostgreSqlAccountRepository;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.CommandLoader;
import br.com.laboon.core.command.CommandProvider;
import br.com.laboon.core.command.CommandScanner;
import br.com.laboon.core.database.DatabaseConfig;
import br.com.laboon.core.database.DatabaseManager;
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
import br.com.laboon.core.redis.RedisManager;
import br.com.laboon.core.report.ReportManager;

import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class LaboonBukkit extends JavaPlugin {

    private static LaboonBukkit instance;

    /*
     * =========================
     * BANCO / CACHE
     * =========================
     */

    private DatabaseManager databaseManager;
    private AccountRepository accountRepository;
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
     * SERVIDOR
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
     * ENABLE
     * =========================
     */

    @Override
    public void onEnable() {

        instance = this;

        getLogger().info("=================================");
        getLogger().info("          LABOON BUKKIT");
        getLogger().info("=================================");

        /*
         * Configuração padrão
         */
        saveDefaultConfig();

        /*
         * Servidor
         */
        loadServerConfig();

        /*
         * Redis
         */
        if (!connectRedis()) {
            disablePlugin(
                    "Redis não está disponível. Plugin será desativado."
            );
            return;
        }

        /*
         * PostgreSQL
         */
        if (!connectDatabase()) {
            disablePlugin(
                    "PostgreSQL não está disponível. Plugin será desativado."
            );
            return;
        }

        /*
         * Componentes dependentes das conexões
         */
        setupRepositories();

        /*
         * Linguagem
         */
        setupLanguage();

        /*
         * Messaging / Profile / Display / Group
         */
        setupMessaging();

        /*
         * Listeners
         */
        registerListeners();

        /*
         * Commands
         */
        registerCommands();

        /*
         * Bukkit APIs
         */
        CooldownAPI.initialize(this);

        startHologramAPI();

        /*
         * Heartbeat
         */
        startHeartbeat();

        if (tabListener != null) {
            tabListener.start();
        }

        if (profileListener != null) {
            profileListener.start();
        }

        /*
         * Action Items
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
         * PostgreSQL Repository
         */
        accountRepository =
                new PostgreSqlAccountRepository(
                        databaseManager
                );

        /*
         * Redis Cache
         */
        accountCache =
                new AccountCache(
                        redisManager
                );

        /*
         * Account Service
         */
        AccountService accountService =
                new AccountService(
                        accountRepository,
                        accountCache
                );

        /*
         * Account Manager
         */
        accountManager =
                new AccountManager(
                        accountService
                );

        /*
         * Statistics
         */
        statisticsRepository =
                new StatisticsRepository(
                        redisManager
                );

        /*
         * Game Coins
         */
        gameCoinsRepository =
                new GameCoinsRepository(
                        redisManager
                );

        /*
         * Reports
         */
        reportManager =
                new ReportManager(
                        redisManager
                );

        /*
         * Friends
         */
        friendManager =
                new FriendManager(
                        redisManager
                );

        /*
         * Skin
         */
        skinService =
                new SkinService(this);

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
         * Profile Provider
         */
        profileProvider =
                new BukkitProfileProvider(
                        accountRepository,
                        statisticsRepository,
                        gameCoinsRepository
                );

        /*
         * Display
         */
        displayManager =
                new DisplayManager(
                        profileProvider,
                        serverConfig
                );

        /*
         * Group Update
         */
        groupUpdateListener =
                new GroupUpdateListener(
                        this,
                        profileProvider,
                        displayManager,
                        messageBus
                );

        groupUpdateListener.register();

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
                        anvilGuiManager
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
        PunishmentChatListener
                punishmentChatListener =
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

    @Override
    public void onDisable() {

        getLogger().info(
                "Desligando Laboon..."
        );

        /*
         * Hologram
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
         * Cooldown
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
         * Display
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
         * Profile
         *
         * Salva os dados antes de fechar
         * o PostgreSQL / Redis.
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
         * Heartbeat
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
         * Profile Listener
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
         * Display Manager
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
         * PostgreSQL
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
         * Redis
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
         * Limpa referências
         */
        accountManager = null;
        accountRepository = null;
        accountCache = null;

        databaseManager = null;
        redisManager = null;

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
}