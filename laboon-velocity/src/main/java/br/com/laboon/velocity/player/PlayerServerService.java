package br.com.laboon.velocity.player;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRegistry;

import com.velocitypowered.api.proxy.Player;

public final class PlayerServerService {

    private final ServerRegistry serverRegistry;

    public PlayerServerService(
            ServerRegistry serverRegistry
    ) {
        this.serverRegistry = serverRegistry;
    }

    public ServerInfo getCurrentServer(
            Player player
    ) {

        if (player == null) {
            return null;
        }

        return player
                .getCurrentServer()
                .map(connection ->
                        connection
                                .getServer()
                                .getServerInfo()
                                .getName()
                )
                .map(serverRegistry::find)
                .orElse(null);
    }
}