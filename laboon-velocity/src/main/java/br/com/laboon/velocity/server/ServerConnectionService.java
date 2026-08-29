package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

public final class ServerConnectionService {

    private final ProxyServer proxyServer;

    public ServerConnectionService(
            ProxyServer proxyServer
    ) {
        this.proxyServer = proxyServer;
    }

    public void connect(
            Player player,
            ServerInfo server
    ) {

        if (player == null || server == null) {
            return;
        }

        proxyServer
                .getServer(server.getName())
                .ifPresentOrElse(
                        registeredServer ->
                                player.createConnectionRequest(
                                        registeredServer
                                ).fireAndForget(),

                        () -> player.sendMessage(
                                net.kyori.adventure.text.Component.text(
                                        "Servidor não encontrado."
                                )
                        )
                );
    }
}