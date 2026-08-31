package br.com.laboon.velocity.listener;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.velocity.server.ServerFallbackService;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public final class ConnectionListener {

    private final ProxyServer proxyServer;
    private final ServerFallbackService fallbackService;

    public ConnectionListener(
            ProxyServer proxyServer,
            ServerFallbackService fallbackService
    ) {
        this.proxyServer =
                proxyServer;

        this.fallbackService =
                fallbackService;
    }

    @Subscribe
    public void onServerPreConnect(
            ServerPreConnectEvent event
    ) {

        // Se já está conectado a algum servidor,
        // não interfere na conexão.
        if (event.getPreviousServer() != null) {
            return;
        }

        ServerInfo lobby =
                fallbackService.findLobby();

        if (lobby == null) {
            return;
        }

        RegisteredServer registeredServer =
                proxyServer
                        .getServer(
                                lobby.getName()
                        )
                        .orElse(null);

        if (registeredServer == null) {
            return;
        }

        event.setResult(
                ServerPreConnectEvent
                        .ServerResult
                        .allowed(
                                registeredServer
                        )
        );
    }

    @Subscribe
    public void onDisconnect(
            DisconnectEvent event
    ) {

        System.out.println(
                "[Laboon] Jogador desconectado: "
                        + event.getPlayer()
                        .getUsername()
        );
    }
}