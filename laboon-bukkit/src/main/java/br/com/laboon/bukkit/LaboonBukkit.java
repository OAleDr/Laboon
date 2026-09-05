package br.com.laboon.bukkit;

import br.com.laboon.bukkit.chat.ChatFormatter;
import br.com.laboon.bukkit.chat.ChatListener;
import br.com.laboon.bukkit.command.BukkitCommandFramework;
import br.com.laboon.bukkit.command.BukkitCommandProvider;
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
import br.com.laboon.bukkit.profile.BukkitProfileProvider;
import br.com.laboon.bukkit.profile.ProfileListener;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.bukkit.server.ServerHeartbeat;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.AccountRepository;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.CommandLoader;
import br.com.laboon.core.command.CommandProvider;
import br.com.laboon.core.command.CommandScanner;
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

    private RedisManager redisManager;
    private MessageBus messageBus;
    private ServerHeartbeat heartbeat;

    private GuiManager guiManager;
    private AnvilGuiManager anvilGuiManager;

    private AccountRepository accountRepository;
    private AccountManager accountManager;

    private StatisticsRepository statisticsRepository;
    private GameCoinsRepository gameCoinsRepository;

    private ProfileProvider profileProvider;

    private ServerConfig serverConfig;

    private DisplayManager displayManager;
    private DisplayListener tabListener;

    private ChatFormatter chatFormatter;

    private LanguageService languageService;

    private BukkitCommandFramework commandFramework;

    private GroupUpdateListener groupUpdateListener;

    private ReportManager reportManager;
    private ReportListGui reportListGui;

    private FriendManager friendManager;
    private FriendGui friendGui;

    @Override
    public void onEnable() {

        instance = this;

        getLogger().info("=================================");
        getLogger().info("          LABOON BUKKIT");
        getLogger().info("=================================");

        saveDefaultConfig();

        loadServerConfig();

        connectRedis();

        if (redisManager == null || !redisManager.isConnected()) {
            return;
        }

        setupLanguage();

        setupMessaging();

        registerListeners();

        registerCommands();

        startHeartbeat();

        if (tabListener != null) {
            tabListener.start();
        }

        getLogger().info("Laboon Bukkit iniciado!");
    }

    private void loadServerConfig() {

        serverConfig = ServerConfigLoader.load(this);

        getLogger().info("Servidor configurado: " + serverConfig.getServerName() + " [" + serverConfig.getServerType() + "/" + serverConfig.getServerRole() + "]");
    }

    private void connectRedis() {

        getLogger().info("Conectando ao Redis...");

        RedisConfig config = RedisConfigLoader.load(this);

        redisManager = new RedisManager(config.getHost(), config.getPort());

        if (!redisManager.isConnected()) {

            getLogger().severe("Não foi possível conectar ao Redis!");

            getServer().getPluginManager().disablePlugin(this);

            return;
        }

        accountRepository = new AccountRepository(redisManager);

        accountManager = new AccountManager(new br.com.laboon.core.account.AccountService(redisManager));

        statisticsRepository = new StatisticsRepository(redisManager);

        gameCoinsRepository = new GameCoinsRepository(redisManager);

        reportManager = new ReportManager(redisManager);

        profileProvider = new BukkitProfileProvider(accountRepository, statisticsRepository, gameCoinsRepository);

        displayManager = new DisplayManager(profileProvider, serverConfig);

        getLogger().info("Redis conectado com sucesso!");
    }

    private void setupLanguage() {

        LanguageModule module = new LanguageModule("paper", getDataFolder().toPath().resolve("languages"));

        languageService = LanguageBootstrap.create(LanguageLocale.ptBR(), List.of(module), List.of(LanguageLocale.ptBR(), LanguageLocale.enUS(), LanguageLocale.esES()));

        Path languages = getDataFolder().toPath().resolve("languages");

        getLogger().info("pt_BR/paper.yml existe: " + Files.exists(languages.resolve("pt_BR").resolve("paper.yml")));

        getLogger().info("en_US/paper.yml existe: " + Files.exists(languages.resolve("en_US").resolve("paper.yml")));

        getLogger().info("es_ES/paper.yml existe: " + Files.exists(languages.resolve("es_ES").resolve("paper.yml")));

        getLogger().info("Sistema de idiomas iniciado.");
    }

    private void registerListeners() {

        guiManager = new GuiManager();

        friendManager = new FriendManager(redisManager);

        anvilGuiManager = new AnvilGuiManager(this);

        friendGui = new FriendGui(guiManager, anvilGuiManager, friendManager, accountManager);

        getServer().getPluginManager().registerEvents(guiManager, this);

        reportListGui = new ReportListGui(guiManager, reportManager, anvilGuiManager);

        getServer().getPluginManager().registerEvents(anvilGuiManager, this);

        getServer().getPluginManager().registerEvents(new ProfileListener(profileProvider), this);

        tabListener = new DisplayListener(this, displayManager);

        getServer().getPluginManager().registerEvents(tabListener, this);

        chatFormatter = new ChatFormatter(profileProvider);

        getServer().getPluginManager().registerEvents(new ChatListener(this, chatFormatter), this);

        getLogger().info("Listeners registrados.");
    }

    private void registerCommands() {

        commandFramework = new BukkitCommandFramework(this, profileProvider);

        CommandProvider commandProvider = new BukkitCommandProvider(guiManager, anvilGuiManager, profileProvider, languageService, reportListGui, friendGui);

        List<Class<? extends CommandClass>> commands = CommandScanner.scan("br.com.laboon.bukkit.command.commands");

        CommandLoader.load(commandFramework, commands, commandProvider);

        getLogger().info("Comandos registrados: " + commands.size());
    }

    private void setupMessaging() {

        RedisPublisher publisher = new RedisPublisher(redisManager);

        RedisSubscriber subscriber = new RedisSubscriber(redisManager);

        messageBus = new MessageBus(publisher, subscriber);

        groupUpdateListener = new GroupUpdateListener(this, profileProvider, displayManager, messageBus);

        groupUpdateListener.register();
    }

    private void startHeartbeat() {

        heartbeat = new ServerHeartbeat(this, redisManager, serverConfig, messageBus);

        heartbeat.start();

        getLogger().info("Heartbeat iniciado: " + serverConfig.getServerName() + " [" + serverConfig.getServerType() + "/" + serverConfig.getServerRole() + "]");
    }

    @Override
    public void onDisable() {

        if (tabListener != null) {
            tabListener.stop();
        }

        if (profileProvider != null) {
            profileProvider.saveAll();
        }

        if (heartbeat != null) {
            heartbeat.stop();
        }

        if (redisManager != null) {
            redisManager.close();
        }

        if (displayManager != null) {
            displayManager.shutdown();
        }

        getLogger().info("Laboon Bukkit encerrado.");

        instance = null;
    }

    public static LaboonBukkit getInstance() {
        return instance;
    }

    public RedisManager getRedisManager() {
        return redisManager;
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

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public FriendGui getFriendGui() {
        return friendGui;
    }
}