package br.com.laboon.bukkit.config;

public final class ServerConfig {

    private final String serverName;
    private final String serverType;

    public ServerConfig(
            String serverName,
            String serverType
    ) {
        this.serverName = serverName;
        this.serverType = serverType;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerType() {
        return serverType;
    }

    public static ServerConfig lobby() {

        return new ServerConfig(
                "LOBBY-01",
                "LOBBY"
        );
    }
}