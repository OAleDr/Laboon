package br.com.laboon.core.server;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ServerRepository {

    private final JedisPooled redis;

    public ServerRepository(RedisManager redisManager) {
        this.redis = redisManager.getJedis();
    }

    public void save(ServerInfo server) {

        if (server == null || server.getName() == null) {
            return;
        }

        String key = "laboon:server:" + server.getName();

        Map<String, String> data = new HashMap<>();

        data.put("name", server.getName());
        data.put("type", enumName(server.getType()));
        data.put("role", enumName(server.getRole()));
        data.put("mode", enumName(server.getMode()));
        data.put("map", safe(server.getMap()));
        data.put("host", safe(server.getHost()));
        data.put("port", String.valueOf(server.getPort()));
        data.put("maxPlayers", String.valueOf(server.getMaxPlayers()));
        data.put("state", enumName(server.getState()));
        data.put("players", String.valueOf(server.getPlayers()));

        redis.hset(key, data);

        /*
         * O registro expira caso o heartbeat
         * deixe de atualizá-lo.
         */
        redis.expire(key, 15);
    }

    public List<ServerInfo> findAll() {

        List<ServerInfo> servers = new ArrayList<>();

        for (String key : redis.keys("laboon:server:*")) {

            String name =
                    key.substring("laboon:server:".length());

            ServerInfo server = findByName(name);

            if (server != null) {
                servers.add(server);
            }
        }

        return servers;
    }

    public ServerInfo findByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        String key = "laboon:server:" + name;

        Map<String, String> data =
                redis.hgetAll(key);

        if (data.isEmpty()) {
            return null;
        }

        String serverName = data.get("name");
        String typeValue = data.get("type");
        String roleValue = data.get("role");
        String host = data.get("host");

        if (serverName == null
                || typeValue == null
                || roleValue == null
                || host == null) {

            return null;
        }

        ServerType type;
        ServerRole role;

        try {

            type = ServerType.valueOf(typeValue);
            role = ServerRole.valueOf(roleValue);

        } catch (IllegalArgumentException exception) {

            return null;
        }

        int port;
        int maxPlayers;
        int players;

        try {

            port = Integer.parseInt(
                    data.getOrDefault("port", "0")
            );

            maxPlayers = Integer.parseInt(
                    data.getOrDefault("maxPlayers", "0")
            );

            players = Integer.parseInt(
                    data.getOrDefault("players", "0")
            );

        } catch (NumberFormatException exception) {

            return null;
        }

        ServerInfo server =
                new ServerInfo(
                        serverName,
                        type,
                        role,
                        host,
                        port,
                        maxPlayers
                );

        /*
         * State
         */
        String stateValue = data.get("state");

        if (stateValue != null && !stateValue.isBlank()) {

            try {

                server.setState(
                        ServerState.valueOf(stateValue)
                );

            } catch (IllegalArgumentException exception) {

                server.setState(ServerState.OFFLINE);
            }
        }

        /*
         * Players
         */
        server.setPlayers(players);

        /*
         * Mode
         *
         * Lobby/Network pode não possuir modo.
         */
        String modeValue = data.get("mode");

        if (modeValue != null && !modeValue.isBlank()) {

            try {

                server.setMode(
                        ServerMode.valueOf(modeValue)
                );

            } catch (IllegalArgumentException exception) {

                server.setMode(null);
            }

        } else {

            server.setMode(null);
        }

        /*
         * Map
         *
         * Lobby/Network pode não possuir mapa.
         */
        String mapValue = data.get("map");

        if (mapValue != null && !mapValue.isBlank()) {
            server.setMap(mapValue);
        } else {
            server.setMap(null);
        }

        return server;
    }

    public List<ServerInfo> findByType(ServerType type) {

        if (type == null) {
            return List.of();
        }

        List<ServerInfo> servers =
                new ArrayList<>();

        for (String key : redis.keys("laboon:server:*")) {

            String name =
                    key.substring("laboon:server:".length());

            ServerInfo server =
                    findByName(name);

            if (server != null
                    && server.getType() == type) {

                servers.add(server);
            }
        }

        return servers;
    }

    public ServerInfo findAvailable(
            ServerType type,
            ServerRole role
    ) {

        if (type == null || role == null) {
            return null;
        }

        return findByTypeAndRole(type, role)
                .stream()
                .filter(server ->
                        server.getState() == ServerState.ONLINE
                                || server.getState() == ServerState.WAITING
                )
                .filter(server ->
                        server.getPlayers()
                                < server.getMaxPlayers()
                )
                .min(
                        (first, second) ->
                                Integer.compare(
                                        first.getPlayers(),
                                        second.getPlayers()
                                )
                )
                .orElse(null);
    }

    public List<ServerInfo> findByRole(ServerRole role) {

        if (role == null) {
            return List.of();
        }

        List<ServerInfo> servers =
                new ArrayList<>();

        for (String key : redis.keys("laboon:server:*")) {

            String name =
                    key.substring("laboon:server:".length());

            ServerInfo server =
                    findByName(name);

            if (server != null
                    && server.getRole() == role) {

                servers.add(server);
            }
        }

        return servers;
    }

    public List<ServerInfo> findByTypeAndRole(
            ServerType type,
            ServerRole role
    ) {

        if (type == null || role == null) {
            return List.of();
        }

        List<ServerInfo> servers =
                new ArrayList<>();

        for (String key : redis.keys("laboon:server:*")) {

            String name =
                    key.substring("laboon:server:".length());

            ServerInfo server =
                    findByName(name);

            if (server != null
                    && server.getType() == type
                    && server.getRole() == role) {

                servers.add(server);
            }
        }

        return servers;
    }

    public void delete(String name) {

        if (name == null || name.isBlank()) {
            return;
        }

        redis.del("laboon:server:" + name);
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