package br.com.laboon.bukkit.config;

import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerType;

public final class ServerConfig {

    private final String serverName;
    private final ServerType serverType;
    private final ServerRole serverRole;

    private final String host;
    private final int port;
    private final int maxPlayers;

    public ServerConfig(
            String serverName,
            ServerType serverType,
            ServerRole serverRole,
            String host,
            int port,
            int maxPlayers
    ) {
        this.serverName = serverName;
        this.serverType = serverType;
        this.serverRole = serverRole;
        this.host = host;
        this.port = port;
        this.maxPlayers = maxPlayers;
    }

    public String getServerName() {
        return serverName;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public ServerRole getServerRole() {
        return serverRole;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}