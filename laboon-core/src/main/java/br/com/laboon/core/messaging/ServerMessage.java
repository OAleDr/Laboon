package br.com.laboon.core.messaging;

import br.com.laboon.core.server.ServerInfo;

public final class ServerMessage {

    private final ServerInfo server;

    public ServerMessage(
            ServerInfo server
    ) {
        this.server = server;
    }

    public ServerInfo getServer() {
        return server;
    }
}