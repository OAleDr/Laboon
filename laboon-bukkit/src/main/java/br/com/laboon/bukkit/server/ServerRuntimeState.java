package br.com.laboon.bukkit.server;

import br.com.laboon.core.server.ServerMode;
import br.com.laboon.core.server.ServerState;

public class ServerRuntimeState {

    private volatile ServerState state = ServerState.WAITING;
    private volatile ServerMode mode;
    private volatile String map;

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
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
}