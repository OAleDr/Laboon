package br.com.laboon.core.server;

import br.com.laboon.core.redis.RedisManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerRegistryTest {

    @Test
    void shouldRegisterAndFindServer() {

        RedisManager redisManager =
                new RedisManager("localhost", 6379);

        ServerRegistry registry =
                new ServerRegistry(redisManager);

        ServerInfo server = new ServerInfo(
                "TEST-01",
                ServerType.BEDWARS,
                "127.0.0.1",
                25571,
                8
        );

        server.setState(ServerState.WAITING);
        server.setPlayers(3);

        registry.register(server);

        ServerInfo result =
                registry.find("TEST-01");

        assertNotNull(result);
        assertEquals("TEST-01", result.getName());
        assertEquals(ServerType.BEDWARS, result.getType());
        assertEquals(ServerState.WAITING, result.getState());
        assertEquals(3, result.getPlayers());

        registry.unregister("TEST-01");
        redisManager.close();
    }
}