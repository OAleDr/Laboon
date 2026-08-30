package br.com.laboon.core.server;

public final class ServerInfoSerializer {

    private ServerInfoSerializer() {
    }

    public static String serialize(
            ServerInfo server
    ) {

        return "name=" + server.getName()
                + ";type=" + server.getType().name()
                + ";host=" + server.getHost()
                + ";port=" + server.getPort()
                + ";maxPlayers=" + server.getMaxPlayers()
                + ";players=" + server.getPlayers()
                + ";state=" + server.getState().name();
    }

    public static ServerInfo deserialize(
            String message
    ) {

        String[] fields =
                message.split(";");

        String name = null;
        ServerType type = null;
        String host = null;

        int port = 0;
        int maxPlayers = 0;
        int players = 0;

        ServerState state =
                ServerState.OFFLINE;

        for (String field : fields) {

            String[] pair =
                    field.split("=", 2);

            if (pair.length != 2) {
                continue;
            }

            String key = pair[0];
            String value = pair[1];

            switch (key) {

                case "name":
                    name = value;
                    break;

                case "type":
                    type =
                            ServerType.valueOf(
                                    value
                            );
                    break;

                case "host":
                    host = value;
                    break;

                case "port":
                    port =
                            Integer.parseInt(
                                    value
                            );
                    break;

                case "maxPlayers":
                    maxPlayers =
                            Integer.parseInt(
                                    value
                            );
                    break;

                case "players":
                    players =
                            Integer.parseInt(
                                    value
                            );
                    break;

                case "state":
                    state =
                            ServerState.valueOf(
                                    value
                            );
                    break;
            }
        }

        if (name == null
                || type == null
                || host == null) {

            return null;
        }

        ServerInfo server =
                new ServerInfo(
                        name,
                        type,
                        host,
                        port,
                        maxPlayers
                );

        server.setPlayers(players);
        server.setState(state);

        return server;
    }
}