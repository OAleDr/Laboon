package br.com.laboon.core.server;

public final class ServerInfo {

    private final String name;
    private final ServerType type;
    private ServerRole role;

    private final String host;
    private final int port;
    private final int maxPlayers;

    private ServerState state;
    private int players;

    /**
     * Modo de jogo deste servidor.
     * <p>
     * Exemplo:
     * SOLO
     * DOUBLES
     * TRIO
     * TEAMS
     */
    private ServerMode mode;

    /**
     * Mapa atual do servidor.
     */
    private String map;

    public ServerInfo(String name, ServerType type, ServerRole role, String host, int port, int maxPlayers) {
        this.name = name;
        this.type = type;
        this.role = role;
        this.host = host;
        this.port = port;
        this.maxPlayers = maxPlayers;

        this.state = ServerState.STARTING;
        this.players = 0;

        this.mode = null;
        this.map = null;
    }

    public String getName() {
        return name;
    }

    public ServerType getType() {
        return type;
    }

    public ServerRole getRole() {
        return role;
    }

    public void setRole(ServerRole role) {
        this.role = role;
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
        this.players = Math.max(0, players);
    }

    public ServerMode getMode() {
        return mode;
    }

    public void setMode(ServerMode mode) {
        this.mode = mode;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    /**
     * Verifica se o servidor possui vagas.
     */
    public boolean hasAvailableSlot() {
        return players < maxPlayers;
    }

    /**
     * Verifica se o servidor está em condições de receber jogadores.
     */
    public boolean isAvailable() {
        if (state == null) {
            return false;
        }

        if (state == ServerState.OFFLINE) {
            return false;
        }

        return hasAvailableSlot();
    }

    @Override
    public String toString() {
        return "ServerInfo{" + "name='" + name + '\'' + ", type=" + type + ", role=" + role + ", host='" + host + '\'' + ", port=" + port + ", maxPlayers=" + maxPlayers + ", state=" + state + ", players=" + players + ", mode=" + mode + ", map='" + map + '\'' + '}';
    }
}