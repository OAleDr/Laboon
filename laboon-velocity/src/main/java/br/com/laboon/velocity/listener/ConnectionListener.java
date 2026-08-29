package br.com.laboon.velocity.listener;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.velocity.server.ServerConnectionService;
import br.com.laboon.velocity.server.ServerSelector;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;

public final class ConnectionListener {

    private final ServerSelector serverSelector;
    private final ServerConnectionService connectionService;

    public ConnectionListener(
            ServerSelector serverSelector,
            ServerConnectionService connectionService
    ) {
        this.serverSelector =
                serverSelector;
        this.connectionService = connectionService;
    }

    @Subscribe
    public void onLogin(
            LoginEvent event
    ) {

        Player player =
                event.getPlayer();

        ServerInfo lobby =
                serverSelector.findLobby();

        if (lobby == null) {

            player.disconnect(
                    net.kyori.adventure.text.Component.text(
                            "Nenhum Lobby disponível."
                    )
            );

            return;
        }

        connectionService.connect(player, lobby);
    }
}