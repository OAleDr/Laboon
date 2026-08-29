package br.com.laboon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;

public final class ServerListener {

    @Subscribe
    public void onPostLogin(
            PostLoginEvent event
    ) {

        String player =
                event.getPlayer()
                        .getUsername();

        System.out.println(
                "[Laboon] Jogador conectado: "
                        + player
        );
    }

    @Subscribe
    public void onDisconnect(
            DisconnectEvent event
    ) {

        String player =
                event.getPlayer()
                        .getUsername();

        System.out.println(
                "[Laboon] Jogador desconectado: "
                        + player
        );
    }
}