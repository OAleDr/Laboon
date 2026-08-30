package br.com.laboon.velocity.command;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerType;
import br.com.laboon.velocity.api.ClickableMessage;
import br.com.laboon.velocity.server.ServerAvailabilityService;
import br.com.laboon.velocity.server.ServerConnectionService;
import br.com.laboon.velocity.server.ServerSelector;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;

import java.util.List;

public final class ServerCommand
        implements SimpleCommand {

    private final ServerSelector serverSelector;
    private final ServerConnectionService connectionService;
    private final ServerAvailabilityService availabilityService;

    public ServerCommand(
            ServerSelector serverSelector,
            ServerConnectionService connectionService,
            ServerAvailabilityService availabilityService
    ) {
        this.serverSelector =
                serverSelector;

        this.connectionService =
                connectionService;

        this.availabilityService =
                availabilityService;
    }

    @Override
    public void execute(
            Invocation invocation
    ) {

        if (!(invocation.source() instanceof Player player)) {

            invocation.source().sendMessage(
                    Component.text(
                            "Apenas jogadores podem utilizar este comando."
                    )
            );

            return;
        }

        String[] arguments =
                invocation.arguments();

        if (arguments.length == 0) {

            showServers(player);

            return;
        }

        String serverName =
                arguments[0];

        ServerInfo server =
                serverSelector.find(
                        serverName
                );

        if (server == null) {

            player.sendMessage(
                    Component.text(
                            "§cServidor não encontrado."
                    )
            );

            return;
        }

        if (!availabilityService.isAvailable(server)) {

            player.sendMessage(
                    Component.text(
                            "§cEste servidor não está disponível no momento."
                    )
            );

            return;
        }

        connectionService.connect(
                player,
                server,
                () -> player.sendMessage(
                        Component.text(
                                "§cNão foi possível conectar ao servidor."
                        )
                )
        );
    }

    @Override
    public List<String> suggest(
            Invocation invocation
    ) {

        String[] arguments =
                invocation.arguments();

        if (arguments.length > 1) {
            return List.of();
        }

        String input =
                arguments.length == 0
                        ? ""
                        : arguments[0].toLowerCase();

        return serverSelector
                .findAll()
                .stream()
                .filter(availabilityService::isAvailable)
                .map(ServerInfo::getName)
                .filter(name ->
                        name.toLowerCase()
                                .startsWith(input)
                )
                .sorted()
                .toList();
    }

    private void showServers(
            Player player
    ) {

        List<ServerInfo> servers =
                serverSelector.findAll()
                        .stream()
                        .filter(server ->
                                server.getRole()
                                        == ServerRole.LOBBY
                        )
                        .toList();

        player.sendMessage(
                Component.text(
                        "§6§l        LABOON NETWORK"
                )
        );

        for (ServerType type :
                ServerType.values()) {

            List<ServerInfo> typeServers =
                    servers.stream()
                            .filter(server ->
                                    server.getType()
                                            == type
                            )
                            .toList();

            if (typeServers.isEmpty()) {
                continue;
            }

            player.sendMessage(
                    Component.text(
                            "§e§l"
                                    + availabilityService
                                    .getTypeName(type)
                    )
            );

            for (ServerInfo server :
                    typeServers) {

                sendServerEntry(
                        player,
                        server
                );
            }
        }
    }

    private void sendServerEntry(
            Player player,
            ServerInfo server
    ) {

        boolean available =
                availabilityService
                        .isAvailable(server);

        String status =
                availabilityService
                        .getStatus(server);

        ClickableMessage message =
                ClickableMessage
                        .text(
                                "§7  "
                                        + server.getName()
                                        + " §f"
                                        + server.getPlayers()
                                        + "/"
                                        + server.getMaxPlayers()
                                        + " "
                                        + status
                        )
                        .hover(
                                available
                                        ? "§eClique para conectar em "
                                        + server.getName()
                                        : "§7Servidor indisponível."
                        );

        if (available) {

            message =
                    message.clickCommand(
                            "/server "
                                    + server.getName()
                    );
        }

        player.sendMessage(
                message.build()
        );
    }
}