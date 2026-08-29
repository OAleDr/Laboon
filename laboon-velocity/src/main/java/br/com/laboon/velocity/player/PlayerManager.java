package br.com.laboon.velocity.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.UUID;

public final class PlayerManager {

    private final ProxyServer proxyServer;

    public PlayerManager(
            ProxyServer proxyServer
    ) {
        this.proxyServer = proxyServer;
    }

    public Player find(UUID uniqueId) {
        return proxyServer
                .getPlayer(uniqueId)
                .orElse(null);
    }

    public Player find(String name) {
        return proxyServer
                .getPlayer(name)
                .orElse(null);
    }

    public int getOnlineCount() {
        return proxyServer
                .getPlayerCount();
    }
}