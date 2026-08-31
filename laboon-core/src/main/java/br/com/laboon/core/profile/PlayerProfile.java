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

    public Statistics getStatistics(String game) {

        String normalized = game.trim().toLowerCase();

        return statistics.computeIfAbsent(normalized, key -> statisticsRepository.find(getUniqueId(), key));
    }

    public void saveStatistics(String game) {

        Statistics stats = statistics.get(game.trim().toLowerCase());

        if (stats == null) {
            return;
        }

        statisticsRepository.save(getUniqueId(), stats);
    }

    public void saveAllStatistics() {

        for (Statistics stats : statistics.values()) {

            statisticsRepository.save(getUniqueId(), stats);
        }
    }
}