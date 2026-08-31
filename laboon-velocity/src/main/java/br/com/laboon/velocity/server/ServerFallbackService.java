package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerType;

import com.velocitypowered.api.proxy.Player;

import java.util.List;

public final class ServerFallbackService {

    private final ServerSelector serverSelector;
    private final ServerConnectionService connectionService;

    public ServerFallbackService(
            ServerSelector serverSelector,
            ServerConnectionService connectionService
    ) {
        this.serverSelector =
                serverSelector;

        this.connectionService =
                connectionService;
    }

    public ServerInfo findLobby() {

        return serverSelector.findLobby();
    }

    public ServerInfo findGameLobby(
            ServerType type
    ) {

        return serverSelector.findGameLobby(
                type
        );
    }

    public void connect(
            Player player,
            ServerType type,
            ServerRole role
    ) {

        connect(
                player,
                type,
                role,
                null
        );
    }

    public void connect(
            Player player,
            ServerType type,
            ServerRole role,
            String excludedServer
    ) {

        List<ServerInfo> servers =
                serverSelector
                        .findAvailableServers(
                                type,
                                role
                        )
                        .stream()
                        .filter(server ->
                                excludedServer == null
                                        || !server.getName()
                                        .equalsIgnoreCase(
                                                excludedServer
                                        )
                        )
                        .toList();

        if (servers.isEmpty()) {
            return;
        }

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