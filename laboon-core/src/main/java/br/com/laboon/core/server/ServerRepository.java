package br.com.laboon.core.server;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ServerRepository {

    private final JedisPooled redis;

    public ServerRepository(RedisManager redisManager) {
        this.redis = redisManager.getJedis();
    }

    public void save(ServerInfo server) {
        String key = "laboon:server:" + server.getName();

        redis.hset(key, Map.of(
                "name", server.getName(),
                "type", server.getType().name(),
                "host", server.getHost(),
                "port", String.valueOf(server.getPort()),
                "maxPlayers", String.valueOf(server.getMaxPlayers()),
                "state", server.getState().name(),
                "players", String.valueOf(server.getPlayers())
        ));
        redis.expire(key, 15);
    }

    public List<ServerInfo> findAll() {

        List<ServerInfo> servers = new ArrayList<>();

        for (String key : redis.keys("laboon:server:*")) {

            String name =
                    key.substring(
                            "laboon:server:".length()
                    );

            ServerInfo server =
                    findByName(name);

            if (server != null) {
                servers.add(server);
            }
        }

        return servers;
    }

    public ServerInfo findByName(String name) {
        String key = "laboon:server:" + name;

        Map<String, String> data = redis.hgetAll(key);

        if (data.isEmpty()) {
            return null;
        }

        ServerInfo server = new ServerInfo(
                data.get("name"),
                ServerType.valueOf(data.get("type")),
                data.get("host"),
                Integer.parseInt(data.get("port")),
                Integer.parseInt(data.get("maxPlayers"))
        );

        server.setState(
                ServerState.valueOf(data.get("state"))
        );

        server.setPlayers(
                Integer.parseInt(data.get("players"))
        );

        return server;
    }

    public List<ServerInfo> findByType(ServerType type) {
        List<ServerInfo> servers = new ArrayList<>();

        for (String key : redis.keys("laboon:server:*")) {
            String name = key.substring("laboon:server:".length());

            ServerInfo server = findByName(name);

            if (server != null && server.getType() == type) {
                servers.add(server);
            }
        }

        return servers;
    }

    public ServerInfo findAvailable(ServerType type) {

        List<ServerInfo> servers = findByType(type);

        System.out.println(
                "[Laboon] Procurando servidor do tipo: "
                        + type
        );

        System.out.println(
                "[Laboon] Servidores encontrados: "
                        + servers.size()
        );

        for (ServerInfo server : servers) {

            System.out.println(
                    "[Laboon] Server: "
                            + server.getName()
                            + " | type="
                            + server.getType()
                            + " | state="
                            + server.getState()
                            + " | players="
                            + server.getPlayers()
                            + " | maxPlayers="
                            + server.getMaxPlayers()
            );
        }

        return servers
                .stream()
                .filter(server ->
                        server.getState() == ServerState.ONLINE
                                || server.getState() == ServerState.WAITING
                )
                .filter(server ->
                        server.getPlayers() < server.getMaxPlayers()
                )
                .min((a, b) -> Integer.compare(
                        a.getPlayers(),
                        b.getPlayers()
                ))
                .orElse(null);
    }

    public void delete(String name) {
        redis.del("laboon:server:" + name);
    }
}