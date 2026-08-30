package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerCache {

    private final Map<String, ServerInfo> servers =
            new ConcurrentHashMap<>();

    public void update(
            ServerInfo server
    ) {

        if (server == null) {
            return;
        }

        servers.put(
                server.getName(),
                server
        );
    }

    public ServerInfo get(
            String name
    ) {

        if (name == null) {
            return null;
        }

        return servers.get(name);
    }

    public void remove(
            String name
    ) {

        if (name == null) {
            return;
        }

        servers.remove(name);
    }

    public boolean contains(
            String name
    ) {

        return name != null
                && servers.containsKey(name);
    }

    public void clear() {

        servers.clear();
    }
}