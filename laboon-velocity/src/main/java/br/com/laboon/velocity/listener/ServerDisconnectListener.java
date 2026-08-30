package br.com.laboon.velocity.listener;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerRegistry;
import br.com.laboon.velocity.server.ServerCache;
import br.com.laboon.velocity.server.ServerFallbackService;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;

public final class ServerDisconnectListener {

    private final ServerRegistry serverRegistry;
    private final ServerCache serverCache;
    private final ServerFallbackService fallbackService;

    public ServerDisconnectListener(
            ServerRegistry serverRegistry,
            ServerCache serverCache,
            ServerFallbackService fallbackService
    ) {
        this.serverRegistry =
                serverRegistry;

        this.serverCache =
                serverCache;

        this.fallbackService =
                fallbackService;
    }

    @Subscribe
    public void onKickedFromServer(
            KickedFromServerEvent event
    ) {

        if (event.kickedDuringServerConnect()) {
            return;
        }

        String serverName =
                event.getServer()
                        .getServerInfo()
                        .getName();

        ServerInfo server =
                serverRegistry.find(
                        serverName
                );

        if (server == null) {

            server =
                    serverCache.get(
                            serverName
                    );
        }

        if (server == null) {
            return;
        }

        if (server.getRole() != ServerRole.LOBBY) {
            return;
        }

        fallbackService.connect(
                event.getPlayer(),
                server.getType(),
                ServerRole.LOBBY,
                server.getName()
        );
    }
}