package br.com.laboon.core.profile;

import br.com.laboon.core.account.Account;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PlayerProfile {

    private final Account account;

    private final StatisticsRepository statisticsRepository;

    private final ConcurrentMap<String, Statistics> statistics = new ConcurrentHashMap<>();

    public PlayerProfile(Account account, StatisticsRepository statisticsRepository) {

        this.account = account;

        this.statisticsRepository = statisticsRepository;
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

    public long getCoins() {

        return account.getCoins();
    }

    public void setCoins(long coins) {

        account.setCoins(coins);
    }

    public void addCoins(long amount) {

        account.setCoins(account.getCoins() + amount);
    }

    public long getExperience() {

        return account.getExperience();
    }

    public void setExperience(long experience) {

        account.setExperience(experience);
    }

    public void addExperience(long amount) {

        account.setExperience(account.getExperience() + amount);
    }

    public Account getAccount() {

        return account;
    }

    /*
     * =========================
     * ESTATÍSTICAS - GERAL
     * =========================
     */

    public Statistics getStatistics(String game) {

        return getStatistics(game, null);
    }

    /*
     * =========================
     * ESTATÍSTICAS - MODO
     * =========================
     */

    public Statistics getStatistics(String game, String mode) {

        String normalizedGame = game.trim().toLowerCase();

        String normalizedMode = mode == null || mode.isBlank() ? null : mode.trim().toLowerCase();

        String cacheKey = normalizedMode == null ? normalizedGame : normalizedGame + ":" + normalizedMode;

        return statistics.computeIfAbsent(cacheKey, key -> statisticsRepository.find(getUniqueId(), normalizedGame, normalizedMode));
    }

    /*
     * =========================
     * SALVAR - GERAL
     * =========================
     */

    public void saveStatistics(String game) {

        saveStatistics(game, null);
    }

    /*
     * =========================
     * SALVAR - MODO
     * =========================
     */

    public void saveStatistics(String game, String mode) {

        String normalizedGame = game.trim().toLowerCase();

        String normalizedMode = mode == null || mode.isBlank() ? null : mode.trim().toLowerCase();

        String cacheKey = normalizedMode == null ? normalizedGame : normalizedGame + ":" + normalizedMode;

        Statistics stats = statistics.get(cacheKey);

        if (stats == null) {
            return;
        }

        statisticsRepository.save(getUniqueId(), stats);
    }

    /*
     * =========================
     * SALVAR TUDO
     * =========================
     */

    public void saveAllStatistics() {

        for (Statistics stats : statistics.values()) {

            statisticsRepository.save(getUniqueId(), stats);
        }
    }
}