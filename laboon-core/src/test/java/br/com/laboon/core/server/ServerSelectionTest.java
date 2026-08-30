package br.com.laboon.core.server;

import br.com.laboon.core.redis.RedisManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerSelectionTest {

    @Test
    void shouldFindBestAvailableServer() {
//
//        RedisManager redisManager =
//                new RedisManager("localhost", 6379);
//
//        ServerRegistry registry =
//                new ServerRegistry(redisManager);
//
////        ServerInfo server1 = new ServerInfo(
////                "BEDWARS-01",
////                ServerType.BEDWARS,
////                "127.0.0.1",
////                25571,
////                8
////        );
//
//        server1.setState(ServerState.WAITING);
//        server1.setPlayers(6);
//
//        ServerInfo server2 = new ServerInfo(
//                "BEDWARS-02",
//                ServerType.BEDWARS,
//                "127.0.0.1",
//                25572,
//                8
//        );
//
//        server2.setState(ServerState.WAITING);
//        server2.setPlayers(2);
//
//        ServerInfo server3 = new ServerInfo(
//                "BEDWARS-03",
//                ServerType.BEDWARS,
//                "127.0.0.1",
//                25573,
//                8
//        );
//
//        server3.setState(ServerState.INGAME);
//        server3.setPlayers(1);
//
//        registry.register(server1);
//        registry.register(server2);
//        registry.register(server3);
//
//        ServerInfo selected =
//                registry.findAvailable(ServerType.BEDWARS);
//
//        assertEquals("BEDWARS-02", selected.getName());
//
//        registry.unregister("BEDWARS-01");
//        registry.unregister("BEDWARS-02");
//        registry.unregister("BEDWARS-03");
//
//        redisManager.close();
    }
}