package br.com.laboon.velocity.listener;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.velocity.server.ServerConnectionService;
import br.com.laboon.velocity.server.ServerFallbackService;
import br.com.laboon.velocity.server.ServerSelector;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;

public final class ConnectionListener {

    private final ServerFallbackService fallbackService;

    public ConnectionListener(
            ServerFallbackService fallbackService
    ) {
        this.fallbackService =
                fallbackService;
    }

    @Subscribe
    public void onPostLogin(
            PostLoginEvent event
    ) {

        fallbackService.connectToLobby(
                event.getPlayer()
        );
    }
}