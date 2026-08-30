package br.com.laboon.core.messaging;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerState;
import br.com.laboon.core.server.ServerType;

import java.util.HashMap;
import java.util.Map;

public final class ServerInfoMessage {

    private ServerInfoMessage() {
    }

    public static Map<String, String> from(
            ServerInfo server
    ) {

        Map<String, String> data =
                new HashMap<>();

        data.put(
                "name",
                server.getName()
        );

        data.put(
                "type",
                server.getType().name()
        );

        data.put(
                "host",
                server.getHost()
        );

        data.put(
                "port",
                String.valueOf(
                        server.getPort()
                )
        );

        data.put(
                "maxPlayers",
                String.valueOf(
                        server.getMaxPlayers()
                )
        );

        data.put(
                "players",
                String.valueOf(
                        server.getPlayers()
                )
        );

        data.put(
                "state",
                server.getState().name()
        );

        return data;
    }
}