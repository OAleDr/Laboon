package br.com.laboon.core.profile;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.redis.RedisManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerProfileTest {

    private static RedisManager redisManager;

    private static GameCoinsRepository gameCoinsRepository;

    private static StatisticsRepository statisticsRepository;

    private static UUID uniqueId;

    @BeforeAll
    static void setUp() {

        redisManager = new RedisManager("localhost", 6379);

        gameCoinsRepository = new GameCoinsRepository(redisManager);

        statisticsRepository = new StatisticsRepository(redisManager);

        uniqueId = UUID.randomUUID();

        redisManager.getJedis().del("laboon:coins:bedwars:" + uniqueId);

        redisManager.getJedis().del("laboon:coins:skywars:" + uniqueId);
    }

    @AfterAll
    static void tearDown() {

        redisManager.getJedis().del("laboon:coins:bedwars:" + uniqueId);

        redisManager.getJedis().del("laboon:coins:skywars:" + uniqueId);

        redisManager.close();
    }

    @Test
    void shouldManageCoinsByGame() {

        Account account = new Account(uniqueId, "TestPlayer");

        PlayerProfile profile = new PlayerProfile(account, statisticsRepository, gameCoinsRepository);

        profile.addCoins("bedwars", 1000L);

        profile.addCoins("skywars", 500L);

        assertEquals(1000L, profile.getCoins("bedwars"));

        assertEquals(500L, profile.getCoins("skywars"));
    }

    @Test
    void shouldRemoveCoins() {

        Account account = new Account(uniqueId, "TestPlayer");

        PlayerProfile profile = new PlayerProfile(account, statisticsRepository, gameCoinsRepository);

        profile.setCoins("bedwars", 1000L);

        profile.removeCoins("bedwars", 200L);

        assertEquals(800L, profile.getCoins("bedwars"));
    }

    @Test
    void shouldNotAllowNegativeCoins() {

        Account account = new Account(uniqueId, "TestPlayer");

        PlayerProfile profile = new PlayerProfile(account, statisticsRepository, gameCoinsRepository);

        profile.setCoins("bedwars", 100L);

        profile.removeCoins("bedwars", 200L);

        assertEquals(0L, profile.getCoins("bedwars"));
    }

    @Test
    void shouldSaveAllCoins() {

        Account account = new Account(uniqueId, "TestPlayer");

        PlayerProfile profile = new PlayerProfile(account, statisticsRepository, gameCoinsRepository);

        profile.setCoins("bedwars", 1500L);

        profile.setCoins("skywars", 750L);

        profile.saveAllCoins();

        assertEquals(1500L, gameCoinsRepository.find(uniqueId, "bedwars"));

        assertEquals(750L, gameCoinsRepository.find(uniqueId, "skywars"));
    }

    @Test
    void shouldLoadCoinsFromRedis() {

        Account account = new Account(uniqueId, "TestPlayer");

        PlayerProfile profile = new PlayerProfile(account, statisticsRepository, gameCoinsRepository);

        profile.setCoins("bedwars", 2000L);
        profile.setCoins("skywars", 1000L);

        profile.saveAllCoins();

        Account loadedAccount = new Account(uniqueId, "TestPlayer");

        PlayerProfile loadedProfile = new PlayerProfile(loadedAccount, statisticsRepository, gameCoinsRepository);

        assertEquals(2000L, loadedProfile.getCoins("bedwars"));

        assertEquals(1000L, loadedProfile.getCoins("skywars"));
    }

}