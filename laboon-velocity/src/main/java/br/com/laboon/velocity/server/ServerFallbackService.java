package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;

import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;

import java.util.List;

public final class ServerFallbackService {

    private final ServerSelector serverSelector;
    private final ServerConnectionService connectionService;
    private final ServerAvailabilityService availabilityService;

    public ServerFallbackService(
            ServerSelector serverSelector,
            ServerConnectionService connectionService,
            ServerAvailabilityService availabilityService
    ) {
        this.serverSelector = serverSelector;
        this.connectionService = connectionService;
        this.availabilityService = availabilityService;
    }

    public void connectToLobby(
            Player player
    ) {

        List<ServerInfo> servers =
                serverSelector
                        .findNetworkLobbies()
                        .stream()
                        .filter(availabilityService::isAvailable)
                        .sorted(
                                (a, b) ->
                                        Integer.compare(
                                                a.getPlayers(),
                                                b.getPlayers()
                                        )
                        )
                        .toList();

        tryNext(
                player,
                servers,
                0
        );
    }

    private void tryNext(
            Player player,
            List<ServerInfo> servers,
            int index
    ) {

        if (index >= servers.size()) {

            player.disconnect(
                    Component.text(
                            "§cNenhum Lobby disponível no momento."
                    )
            );

            return;
        }

        ServerInfo server =
                servers.get(index);

        connectionService.connect(
                player,
                server,
                () -> tryNext(
                        player,
                        servers,
                        index + 1
                )
        );
    }
}