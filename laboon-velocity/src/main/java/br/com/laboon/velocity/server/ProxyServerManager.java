package br.com.laboon.velocity.server;

import br.com.laboon.core.server.*;

import java.util.List;

public final class ProxyServerManager {

    private final ServerRegistry registry;

    public ProxyServerManager(
            ServerRegistry registry
    ) {
        this.registry = registry;
    }

    public ServerInfo find(
            String name
    ) {

        return registry.find(name);
    }

    public List<ServerInfo> findAll() {

        return registry.findAll();
    }

    public List<ServerInfo> findByType(
            ServerType type
    ) {

        return registry.findByType(type);
    }

    public ServerInfo findAvailable(
            ServerType type,
            ServerRole role
    ) {

        return registry.findAvailable(type, role);
    }

    public List<ServerInfo> findByTypeAndRole(
            ServerType type,
            ServerRole role
    ) {
        return registry.findByTypeAndRole(
                type,
                role
        );
    }

}