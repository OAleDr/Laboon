package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRegistry;
import br.com.laboon.core.server.ServerState;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerRegistrationService {

    private final ProxyServer proxyServer;
    private final ServerRegistry registry;

    private final Map<String, RegisteredServer> registeredServers =
            new ConcurrentHashMap<>();

    public ServerRegistrationService(
            ProxyServer proxyServer,
            ServerRegistry registry
    ) {
        this.proxyServer = proxyServer;
        this.registry = registry;
    }

    public void registerAll() {

        for (ServerInfo server : registry.findAll()) {
            register(server);
        }
    }

    public void register(ServerInfo server) {

        if (server == null) {
            return;
        }

        if (server.getState() == ServerState.OFFLINE) {
            return;
        }

        if (registeredServers.containsKey(server.getName())) {
            return;
        }

        if (proxyServer
                .getServer(server.getName())
                .isPresent()) {

            RegisteredServer existing =
                    proxyServer
                            .getServer(server.getName())
                            .get();

            registeredServers.put(
                    server.getName(),
                    existing
            );

            return;
        }

        InetSocketAddress address =
                new InetSocketAddress(
                        server.getHost(),
                        server.getPort()
                );

        com.velocitypowered.api.proxy.server.ServerInfo velocityInfo =
                new com.velocitypowered.api.proxy.server.ServerInfo(
                        server.getName(),
                        address
                );

        RegisteredServer registeredServer =
                proxyServer.registerServer(
                        velocityInfo
                );

        registeredServers.put(
                server.getName(),
                registeredServer
        );

        System.out.println(
                "[Laboon] Servidor registrado: "
                        + server.getName()
                        + " -> "
                        + server.getHost()
                        + ":"
                        + server.getPort()
                        + " ["
                        + server.getType()
                        + "]"
        );
    }

    public void unregister(String name) {

        if (name == null) {
            return;
        }

        RegisteredServer server =
                registeredServers.remove(name);

        if (server == null) {
            return;
        }

        proxyServer.unregisterServer(
                server.getServerInfo()
        );

        System.out.println(
                "[Laboon] Servidor removido: "
                        + name
        );
    }

    public Set<String> getRegisteredNames() {
        return new HashSet<>(
                registeredServers.keySet()
        );
    }

    public RegisteredServer get(String name) {

        return registeredServers.get(name);
    }

    public boolean isRegistered(String name) {

        return registeredServers.containsKey(name);
    }
}