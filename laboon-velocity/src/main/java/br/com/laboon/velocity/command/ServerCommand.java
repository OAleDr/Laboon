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

            player.sendMessage(
                    net.kyori.adventure.text.Component.text(
                            "Use: /server <servidor>"
                    )
            );

            return;
        }

        String serverName =
                arguments[0];

        ServerInfo server =
                serverSelector
                        .find(serverName);

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
}