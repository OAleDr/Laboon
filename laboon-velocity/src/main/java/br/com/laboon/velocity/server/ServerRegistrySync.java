package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ServerRegistrySync {

    private final ServerRegistry registry;
    private final ServerRegistrationService registrationService;
    private final ServerCache serverCache;

    public ServerRegistrySync(
            ServerRegistry registry,
            ServerRegistrationService registrationService,
            ServerCache serverCache
    ) {
        this.registry =
                registry;

        this.registrationService =
                registrationService;

        this.serverCache =
                serverCache;
    }

    public void sync() {

        List<ServerInfo> servers =
                registry.findAll();

        Set<String> currentServers =
                new HashSet<>();

        for (ServerInfo server : servers) {

            if (server == null) {
                continue;
            }

            currentServers.add(
                    server.getName()
            );

            serverCache.update(server);

            registrationService.register(server);
        }

        removeOfflineServers(
                currentServers
        );
    }

    private void removeOfflineServers(
            Set<String> currentServers
    ) {

        Set<String> registered =
                registrationService.getRegisteredNames();

        for (String name : registered) {

            if (currentServers.contains(name)) {
                continue;
            }

            registrationService.unregister(name);
        }
    }
}