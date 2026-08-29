package br.com.laboon.core.server;

public final class ServerInfo {

    private final String name;
    private final ServerType type;
    private final String host;
    private final int port;
    private final int maxPlayers;

    private ServerState state;
    private int players;

    public ServerInfo(
            String name,
            ServerType type,
            String host,
            int port,
            int maxPlayers
    ) {
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
        this.maxPlayers = maxPlayers;
        this.state = ServerState.STARTING;
        this.players = 0;
    }

    public String getName() {
        return name;
    }

    public ServerType getType() {
        return type;
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

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }
}