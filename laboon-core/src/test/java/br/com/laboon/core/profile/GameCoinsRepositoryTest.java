package br.com.laboon.core.profile;

import br.com.laboon.core.redis.RedisManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameCoinsRepositoryTest {

    private static RedisManager redisManager;
    private static GameCoinsRepository repository;

    private static UUID uniqueId;

    @BeforeAll
    static void setUp() {

        redisManager = new RedisManager("localhost", 6379);

        repository = new GameCoinsRepository(redisManager);

        uniqueId = UUID.randomUUID();

        redisManager.getJedis().del("laboon:coins:bedwars:" + uniqueId);
    }

    @AfterAll
    static void tearDown() {

        redisManager.getJedis().del("laboon:coins:bedwars:" + uniqueId);

        redisManager.close();
    }

    @Test
    void shouldSaveAndFindCoins() {

        repository.save(uniqueId, "bedwars", 1000L);

        long coins = repository.find(uniqueId, "bedwars");

        assertEquals(1000L, coins);
    }

    @Test
    void shouldReturnZeroWhenCoinsDoNotExist() {

        UUID newUniqueId = UUID.randomUUID();

        long coins = repository.find(newUniqueId, "bedwars");

        assertEquals(0L, coins);
    }

    @Test
    void shouldCheckIfCoinsExist() {

        repository.save(uniqueId, "bedwars", 500L);

        assertTrue(repository.exists(uniqueId, "bedwars"));
    }

    @Test
    void shouldDeleteCoins() {

        repository.save(uniqueId, "bedwars", 750L);

        repository.delete(uniqueId, "bedwars");

        assertFalse(repository.exists(uniqueId, "bedwars"));

        assertEquals(0L, repository.find(uniqueId, "bedwars"));
    }

    @Test
    void shouldKeepCoinsSeparatedByGame() {

        repository.save(uniqueId, "bedwars", 1000L);

        repository.save(uniqueId, "skywars", 2500L);

        assertEquals(1000L, repository.find(uniqueId, "bedwars"));

        assertEquals(2500L, repository.find(uniqueId, "skywars"));

        repository.delete(uniqueId, "bedwars");

        repository.delete(uniqueId, "skywars");
    }
}