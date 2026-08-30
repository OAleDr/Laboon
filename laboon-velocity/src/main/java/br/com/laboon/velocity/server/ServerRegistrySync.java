package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRegistry;

import java.util.HashSet;
import java.util.Set;

public final class ServerRegistrySync {

    private final ServerRegistry registry;
    private final ServerRegistrationService registrationService;

    public ServerRegistrySync(
            ServerRegistry registry,
            ServerRegistrationService registrationService
    ) {
        this.registry = registry;
        this.registrationService =
                registrationService;
    }

    public void sync() {

        Set<String> activeServers =
                new HashSet<>();

        for (ServerInfo server :
                registry.findAll()) {

            activeServers.add(
                    server.getName()
            );

            registrationService.register(
                    server
            );
        }

        Set<String> registeredServers =
                registrationService
                        .getRegisteredNames();

        for (String registered :
                registeredServers) {

            if (!activeServers.contains(
                    registered
            )) {

                registrationService.unregister(
                        registered
                );
            }
        }
    }
}