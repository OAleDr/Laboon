package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerState;
import br.com.laboon.core.server.ServerType;

import java.util.List;

public final class ServerSelector {

    private final ProxyServerManager serverManager;
    private final  ServerAvailabilityService availabilityService;

    public ServerSelector(
            ProxyServerManager serverManager,
            ServerAvailabilityService availabilityService
    ) {
        this.serverManager = serverManager;
        this.availabilityService = availabilityService;
    }

    public ServerInfo find(
            String name
    ) {

        return serverManager.find(name);
    }

    public ServerInfo findLobby() {

        return serverManager.findAvailable(
                ServerType.NETWORK,
                ServerRole.LOBBY
        );
    }

    public List<ServerInfo> findNetworkLobbies() {

        return serverManager.findByTypeAndRole(
                ServerType.NETWORK,
                ServerRole.LOBBY
        );
    }

    public List<ServerInfo> findGameLobbies(
            ServerType type
    ) {

        return serverManager
                .findByTypeAndRole(
                        type,
                        ServerRole.LOBBY
                )
                .stream()
                .filter(
                        availabilityService::isAvailable
                )
                .sorted(
                        (a, b) ->
                                Integer.compare(
                                        a.getPlayers(),
                                        b.getPlayers()
                                )
                )
                .toList();
    }

    public ServerInfo findGameLobby(
            ServerType type
    ) {

        return serverManager.findAvailable(
                type,
                ServerRole.LOBBY
        );
    }

    public List<ServerInfo> findAll() {

        return serverManager.findAll();
    }

}