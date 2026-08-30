package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerType;

import java.util.List;

public final class ServerSelector {

    private final ProxyServerManager serverManager;

    public ServerSelector(
            ProxyServerManager serverManager
    ) {
        this.serverManager = serverManager;
    }

    public ServerInfo find(
            String name
    ) {

        return serverManager.find(name);
    }

    public ServerInfo findLobby() {

        return serverManager
                .findAvailable(ServerType.LOBBY);
    }

    public ServerInfo findGameServer(
            ServerType type
    ) {

        return serverManager.findAvailable(
                type
        );
    }

    public List<ServerInfo> findAll() {

        return serverManager.findAll();
    }

}