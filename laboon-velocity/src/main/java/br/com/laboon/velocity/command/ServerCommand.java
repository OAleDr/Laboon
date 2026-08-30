package br.com.laboon.velocity.command;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.velocity.player.PlayerManager;
import br.com.laboon.velocity.server.ServerConnectionService;
import br.com.laboon.velocity.server.ServerSelector;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public final class ServerCommand
        implements SimpleCommand {

    private final PlayerManager playerManager;
    private final ServerSelector serverSelector;
    private final ServerConnectionService connectionService;

    public ServerCommand(
            PlayerManager playerManager,
            ServerSelector serverSelector,
            ServerConnectionService connectionService
    ) {
        this.playerManager = playerManager;
        this.serverSelector = serverSelector;
        this.connectionService = connectionService;
    }

    @Override
    public void execute(
            Invocation invocation
    ) {

        if (!(invocation.source() instanceof Player player)) {

            invocation.source().sendMessage(
                    net.kyori.adventure.text.Component.text(
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
                serverSelector.find(serverName);

        if (server == null) {

            player.sendMessage(
                    net.kyori.adventure.text.Component.text(
                            "Servidor não encontrado."
                    )
            );

            return;
        }

        connectionService.connect(
                player,
                server
        );
    }

    private void showServers(
            Player player
    ) {

        player.sendMessage(
                net.kyori.adventure.text.Component.text(
                        "§6§lServidores disponíveis"
                )
        );

        for (ServerInfo server :
                serverSelector.findAll()) {

            player.sendMessage(
                    net.kyori.adventure.text.Component.text(
                            "§e"
                                    + server.getName()
                                    + " §7- §f"
                                    + server.getPlayers()
                                    + "/"
                                    + server.getMaxPlayers()
                                    + " §8["
                                    + server.getState()
                                    + "]"
                    )
            );
        }

        player.sendMessage(
                net.kyori.adventure.text.Component.text(
                        "§7Use §f/server <servidor> §7para conectar."
                )
        );
    }
}