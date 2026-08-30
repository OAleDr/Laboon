package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.adventure.text.Component;

public final class ServerConnectionService {

    private final ProxyServer proxyServer;
    private final ServerAvailabilityService availabilityService;

    public ServerConnectionService(
            ProxyServer proxyServer,
            ServerAvailabilityService availabilityService
    ) {
        this.proxyServer = proxyServer;
        this.availabilityService = availabilityService;
    }

    public void connect(
            Player player,
            ServerInfo server,
            Runnable onFailure
    ) {

        if (player == null || server == null) {
            return;
        }

        RegisteredServer registeredServer =
                proxyServer
                        .getServer(server.getName())
                        .orElse(null);

        if (registeredServer == null) {

            onFailure.run();

            return;
        }

        if (!availabilityService.isAvailable(server)) {

            onFailure.run();

            return;
        }

        if (player.getCurrentServer()
                .map(connection ->
                        connection.getServer()
                                .getServerInfo()
                                .getName()
                                .equalsIgnoreCase(
                                        server.getName()
                                )
                )
                .orElse(false)) {

            return;
        }

        player.createConnectionRequest(
                registeredServer
        ).connect().whenComplete(
                (result, throwable) -> {

                    if (throwable != null ||
                            !result.isSuccessful()) {

                        onFailure.run();
                    }
                }
        );
    }
}