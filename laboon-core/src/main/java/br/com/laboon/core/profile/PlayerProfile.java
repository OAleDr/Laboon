package br.com.laboon.core.profile;

import br.com.laboon.core.account.Account;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PlayerProfile {

    private final Account account;
    private final StatisticsRepository statisticsRepository;
    private final GameCoinsRepository gameCoinsRepository;

    private final ConcurrentMap<String, Statistics> statistics = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Long> gameCoins = new ConcurrentHashMap<>();

    public PlayerProfile(Account account, StatisticsRepository statisticsRepository, GameCoinsRepository gameCoinsRepository) {
        if (account == null) {
            throw new IllegalArgumentException("Account não pode ser nulo.");
        }

        if (statisticsRepository == null) {
            throw new IllegalArgumentException("StatisticsRepository não pode ser nulo.");
        }

        if (gameCoinsRepository == null) {
            throw new IllegalArgumentException("GameCoinsRepository não pode ser nulo.");
        }

        this.account = account;
        this.statisticsRepository = statisticsRepository;
        this.gameCoinsRepository = gameCoinsRepository;
    }

    public UUID getUniqueId() {
        return account.getUniqueId();
    }

    public String getName() {
        return account.getName();
    }

    public String getRank() {
        return account.getRank();
    }

    public void setRank(String rank) {
        account.setRank(rank);
    }

    public long getExperience() {
        return account.getExperience();
    }

    public void setExperience(long experience) {
        account.setExperience(experience);
    }

    public void addExperience(long amount) {
        if (amount <= 0) {
            return;
        }

        account.setExperience(account.getExperience() + amount);
    }

    public Account getAccount() {
        return account;
    }

    /*
     * =========================
     * STATISTICS
     * =========================
     */

    public Statistics getStatistics(String game) {
        return getStatistics(game, null);
    }

    public Statistics getStatistics(String game, String mode) {
        String normalizedGame = normalize(game);
        String normalizedMode = normalizeNullable(mode);

        String cacheKey = createStatisticsCacheKey(normalizedGame, normalizedMode);

        return statistics.computeIfAbsent(cacheKey, key -> statisticsRepository.find(getUniqueId(), normalizedGame, normalizedMode));
    }

    public void saveStatistics(String game) {
        saveStatistics(game, null);
    }

    public void saveStatistics(String game, String mode) {
        String normalizedGame = normalize(game);
        String normalizedMode = normalizeNullable(mode);

        String cacheKey = createStatisticsCacheKey(normalizedGame, normalizedMode);

        Statistics value = statistics.get(cacheKey);

        if (value == null) {
            return;
        }

        statisticsRepository.save(getUniqueId(), value);
    }

    public void saveAllStatistics() {
        for (Statistics value : statistics.values()) {
            statisticsRepository.save(getUniqueId(), value);
        }
    }

    /*
     * =========================
     * GAME COINS
     * =========================
     */

    public long getCoins(String game) {
        String normalizedGame = normalize(game);

        return gameCoins.computeIfAbsent(normalizedGame, key -> gameCoinsRepository.find(getUniqueId(), normalizedGame));
    }

    public void setCoins(String game, long coins) {
        String normalizedGame = normalize(game);

        gameCoins.put(normalizedGame, Math.max(0L, coins));
    }

    public void addCoins(String game, long amount) {
        if (amount <= 0) {
            return;
        }

        String normalizedGame = normalize(game);

        gameCoins.merge(normalizedGame, amount, Long::sum);
    }

    public void removeCoins(String game, long amount) {
        if (amount <= 0) {
            return;
        }

        String normalizedGame = normalize(game);

        gameCoins.compute(normalizedGame, (key, current) -> {
            long coins = current == null ? gameCoinsRepository.find(getUniqueId(), normalizedGame) : current;

            return Math.max(0L, coins - amount);
        });
    }

    public void saveCoins(String game) {
        String normalizedGame = normalize(game);

        Long coins = gameCoins.get(normalizedGame);

        if (coins == null) {
            return;
        }

        gameCoinsRepository.save(getUniqueId(), normalizedGame, coins);
    }

    public void saveAllCoins() {
        for (Map.Entry<String, Long> entry : gameCoins.entrySet()) {
            gameCoinsRepository.save(getUniqueId(), entry.getKey(), entry.getValue());
        }
    }

    /*
     * =========================
     * SAVE ALL
     * =========================
     */

    public void saveAll() {
        saveAllStatistics();
        saveAllCoins();
    }

    /*
     * =========================
     * INTERNAL
     * =========================
     */

    private String createStatisticsCacheKey(String game, String mode) {
        if (mode == null) {
            return game;
        }

        return game + ":" + mode;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O valor não pode ser vazio.");
        }

        return value.trim().toLowerCase();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }
}