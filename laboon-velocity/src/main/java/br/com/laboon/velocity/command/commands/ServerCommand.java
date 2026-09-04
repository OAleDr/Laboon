package br.com.laboon.velocity.command.commands;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerType;
import br.com.laboon.velocity.account.VelocityAccountService;
import br.com.laboon.velocity.api.ClickableMessage;
import br.com.laboon.velocity.server.ServerAvailabilityService;
import br.com.laboon.velocity.server.ServerConnectionService;
import br.com.laboon.velocity.server.ServerSelector;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;

public final class ServerCommand implements SimpleCommand {

    private final ServerSelector serverSelector;
    private final ServerConnectionService connectionService;
    private final ServerAvailabilityService availabilityService;
    private final LanguageService languageService;
    private final VelocityAccountService accountService;

    public ServerCommand(ServerSelector serverSelector, ServerConnectionService connectionService, ServerAvailabilityService availabilityService, LanguageService languageService, VelocityAccountService accountService) {

        this.serverSelector = serverSelector;

        this.connectionService = connectionService;

        this.availabilityService = availabilityService;

        this.languageService = languageService;

        this.accountService = accountService;
    }

    @Override
    public void execute(Invocation invocation) {

        if (!(invocation.source() instanceof Player player)) {

            invocation.source().sendMessage(Component.text("Apenas jogadores podem utilizar este comando."));

            return;
        }

        String[] arguments = invocation.arguments();

        if (arguments.length == 0) {

            showServers(player);

            return;
        }

        String serverName = arguments[0];

        ServerInfo server = serverSelector.find(serverName);

        if (server == null) {

            player.sendMessage(message(player, "server.not-found"));

            return;
        }

        if (!availabilityService.isAvailable(server)) {

            player.sendMessage(message(player, "server.unavailable"));

            return;
        }

        connectionService.connect(player, server, () -> player.sendMessage(message(player, "server.connection-failed")));
    }

    @Override
    public List<String> suggest(Invocation invocation) {

        String[] arguments = invocation.arguments();

        if (arguments.length > 1) {
            return List.of();
        }

        String input = arguments.length == 0 ? "" : arguments[0].toLowerCase();

        return serverSelector.findAll().stream().filter(availabilityService::isAvailable).map(ServerInfo::getName).filter(name -> name.toLowerCase().startsWith(input)).sorted().toList();
    }

    private void showServers(Player player) {

        List<ServerInfo> servers = serverSelector.findAll().stream().filter(server -> server.getRole() == ServerRole.LOBBY).toList();

        player.sendMessage(Component.text("§6§l        LABOON NETWORK"));

        for (ServerType type : ServerType.values()) {

            List<ServerInfo> typeServers = servers.stream().filter(server -> server.getType() == type).toList();

            if (typeServers.isEmpty()) {
                continue;
            }

            player.sendMessage(Component.text("§e§l" + availabilityService.getTypeName(type)));

            for (ServerInfo server : typeServers) {

                sendServerEntry(player, server);
            }
        }
    }

    private void sendServerEntry(Player player, ServerInfo server) {

        LanguageLocale locale = getPlayerLocale(player);

        boolean currentServer = player.getCurrentServer().map(connection -> connection.getServer().getServerInfo().getName().equalsIgnoreCase(server.getName())).orElse(false);

        boolean available = !currentServer && availabilityService.isAvailable(server);

        String statusKey = availabilityService.getStatus(server);

        Component status = languageService.message(locale, "velocity", statusKey);

        String entryKey;

        if (currentServer) {

            entryKey = "server.entry.current";

        } else if (available) {

            entryKey = "server.entry.available";

        } else {

            entryKey = "server.entry.unavailable";
        }

        Component entry = languageService.message(locale, "velocity", entryKey);

        Component text = Component.text().append(Component.text("  ")).append(Component.text(server.getName())).append(Component.text(" ")).append(Component.text(server.getPlayers() + "/" + server.getMaxPlayers())).append(Component.text(" ")).append(status).append(Component.text(" ")).append(entry).build();

        Component hover;

        if (currentServer) {

            hover = languageService.message(locale, "velocity", "server.hover.current");

        } else if (available) {

            hover = languageService.message(locale, "velocity", "server.hover.connect", Map.of("server", server.getName()));

        } else {

            hover = languageService.message(locale, "velocity", "server.hover.unavailable");
        }

        ClickableMessage message = ClickableMessage.text(text).hover(hover);

        if (available) {

            message = message.clickCommand("/server " + server.getName());
        }

        player.sendMessage(message.build());
    }

    private Component message(Player player, String key) {

        return languageService.message(getPlayerLocale(player), "velocity", key);
    }

    private LanguageLocale getPlayerLocale(Player player) {

        Account account = accountService.getAccount(player);

        if (account == null) {

            return LanguageLocale.ptBR();
        }

        return account.getPreferences().getLanguage();
    }
}