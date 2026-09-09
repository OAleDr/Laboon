package br.com.laboon.core.server;

public final class ServerInfoSerializer {

    private ServerInfoSerializer() {
    }

    public static String serialize(ServerInfo server) {

        if (server == null) {
            return "";
        }

        return "name=" + safe(server.getName())
                + ";type=" + enumName(server.getType())
                + ";role=" + enumName(server.getRole())
                + ";mode=" + enumName(server.getMode())
                + ";map=" + safe(server.getMap())
                + ";host=" + safe(server.getHost())
                + ";port=" + server.getPort()
                + ";maxPlayers=" + server.getMaxPlayers()
                + ";players=" + server.getPlayers()
                + ";state=" + enumName(server.getState());
    }

    public static ServerInfo deserialize(String message) {

        if (message == null || message.isBlank()) {
            return null;
        }

        String[] fields = message.split(";");

        String name = null;
        ServerType type = null;
        ServerRole role = null;
        ServerMode mode = null;
        String map = null;
        String host = null;

        int port = 0;
        int maxPlayers = 0;
        int players = 0;

        ServerState state = ServerState.OFFLINE;

        for (String field : fields) {

            String[] pair = field.split("=", 2);

            if (pair.length != 2) {
                continue;
            }

            String key = pair[0];
            String value = pair[1];

            if (value.isBlank()) {
                value = null;
            }

            try {

                switch (key) {

                    case "name":
                        name = value;
                        break;

                    case "type":
                        if (value != null) {
                            type = ServerType.valueOf(value);
                        }
                        break;

                    case "role":
                        if (value != null) {
                            role = ServerRole.valueOf(value);
                        }
                        break;

                    case "mode":
                        if (value != null) {
                            mode = ServerMode.valueOf(value);
                        }
                        break;

                    case "map":
                        map = value;
                        break;

                    case "host":
                        host = value;
                        break;

                    case "port":
                        if (value != null) {
                            port = Integer.parseInt(value);
                        }
                        break;

                    case "maxPlayers":
                        if (value != null) {
                            maxPlayers = Integer.parseInt(value);
                        }
                        break;

                    case "players":
                        if (value != null) {
                            players = Integer.parseInt(value);
                        }
                        break;

                    case "state":
                        if (value != null) {
                            state = ServerState.valueOf(value);
                        }
                        break;
                }

            } catch (IllegalArgumentException exception) {

                return null;
            }
        }

        if (name == null
                || type == null
                || host == null) {

            return null;
        }

        ServerInfo server = new ServerInfo(
                name,
                type,
                role,
                host,
                port,
                maxPlayers
        );

        server.setMode(mode);
        server.setMap(map);

        server.setPlayers(players);
        server.setState(state);

        return server;
    }

    private static String enumName(Enum<?> value) {

        return value == null
                ? ""
                : value.name();
    }

    private static String safe(String value) {

        return value == null
                ? ""
                : value;
    }
}