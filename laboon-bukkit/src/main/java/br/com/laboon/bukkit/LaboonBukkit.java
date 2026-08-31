package br.com.laboon.bukkit;

import br.com.laboon.bukkit.command.GuiTestCommand;
import br.com.laboon.bukkit.command.ProfileCommand;
import br.com.laboon.bukkit.config.RedisConfig;
import br.com.laboon.bukkit.config.RedisConfigLoader;
import br.com.laboon.bukkit.config.ServerConfig;
import br.com.laboon.bukkit.config.ServerConfigLoader;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.bukkit.profile.BukkitProfileProvider;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.bukkit.server.ServerHeartbeat;
import br.com.laboon.core.account.AccountRepository;
import br.com.laboon.core.language.LanguageBootstrap;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageModule;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.RedisPublisher;
import br.com.laboon.core.messaging.RedisSubscriber;
import br.com.laboon.core.profile.StatisticsRepository;
import br.com.laboon.core.redis.RedisManager;
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

    private AccountRepository accountRepository;

    private StatisticsRepository statisticsRepository;

    private ProfileProvider profileProvider;

    private LanguageService languageService;


    @Override
    public void onEnable() {

        instance = this;

        getLogger().info("=================================");
        getLogger().info("          LABOON BUKKIT");
        getLogger().info("=================================");

        connectRedis();

        if (redisManager == null) {
            return;
        }

        setupLanguage();

        registerListeners();

        setupMessaging();

        saveDefaultConfig();

        registerCommands();

        startHeartbeat();

        getLogger().info("Laboon Bukkit iniciado!");
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

        statisticsRepository = new StatisticsRepository(redisManager);

        profileProvider = new BukkitProfileProvider(accountRepository, statisticsRepository);

        getLogger().info("Redis conectado com sucesso!");
    }

    private void setupLanguageFiles() {

        saveResource(
                "languages/pt_BR.yml",
                false
        );

        saveResource(
                "languages/en_US.yml",
                false
        );

        saveResource(
                "languages/es_ES.yml",
                false
        );
    }

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


    private void registerListeners() {

        guiManager = new GuiManager();

        getServer().getPluginManager().registerEvents(guiManager, this);

        getLogger().info("Listeners registrados.");
    }


    private void registerCommands() {

        if (getCommand("profile") == null) {

            getLogger().warning("Comando /profile não encontrado no plugin.yml.");

        } else {

            getCommand("profile").setExecutor(new ProfileCommand(guiManager, profileProvider, languageService));
        }


        if (getCommand("guitest") == null) {

            getLogger().warning("Comando /guitest não encontrado no plugin.yml.");

        } else {

            getCommand("guitest").setExecutor(new GuiTestCommand(guiManager));
        }

        getLogger().info("Comandos registrados.");
    }


    private void setupMessaging() {

        RedisPublisher publisher = new RedisPublisher(redisManager);

        RedisSubscriber subscriber = new RedisSubscriber(redisManager);

        messageBus = new MessageBus(publisher, subscriber);
    }


    private void startHeartbeat() {

        ServerConfig config = ServerConfigLoader.load(this);

        heartbeat = new ServerHeartbeat(this, redisManager, config, messageBus);

        heartbeat.start();

        getLogger().info("Heartbeat iniciado: " + config.getServerName() + " [" + config.getServerType() + "/" + config.getServerRole() + "]");
    }


    @Override
    public void onDisable() {

        if (heartbeat != null) {

            heartbeat.stop();
        }

        if (redisManager != null) {

            redisManager.close();
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


    public LanguageService getLanguageService() {

        return languageService;
    }


    public ProfileProvider getProfileProvider() {

        return profileProvider;
    }
}