package br.com.laboon.core.server;

import br.com.laboon.core.redis.RedisManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServerHeartbeatTest {

    @Test
    void shouldKeepServerRegistered() throws InterruptedException {

        RedisManager redisManager =
                new RedisManager("localhost", 6379);

        ServerInfo server = new ServerInfo(
                "HEARTBEAT-TEST",
                ServerType.BEDWARS,
                "127.0.0.1",
                25571,
                8
        );

        ServerHeartbeat heartbeat =
                new ServerHeartbeat(server, redisManager);

        heartbeat.start();

        Thread.sleep(1000);

        ServerRegistry registry =
                new ServerRegistry(redisManager);

        ServerInfo result =
                registry.find("HEARTBEAT-TEST");

        assertNotNull(result);

        heartbeat.stop();
        redisManager.close();
    }
}