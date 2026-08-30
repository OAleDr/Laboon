package br.com.laboon.velocity.listener;

import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerType;
import br.com.laboon.velocity.server.ServerFallbackService;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;

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

        fallbackService.connect(
                event.getPlayer(),
                ServerType.NETWORK,
                ServerRole.LOBBY
        );
    }
}