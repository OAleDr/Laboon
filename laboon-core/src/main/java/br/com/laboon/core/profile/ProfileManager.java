package br.com.laboon.core.profile;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ProfileManager {

    private final AccountManager accountManager;

    private final StatisticsRepository statisticsRepository;

    private final ConcurrentMap<UUID, PlayerProfile> profiles =
            new ConcurrentHashMap<>();

    public ProfileManager(
            AccountManager accountManager,
            StatisticsRepository statisticsRepository
    ) {

        this.accountManager =
                accountManager;

        this.statisticsRepository =
                statisticsRepository;
    }

    public PlayerProfile get(
            UUID uniqueId
    ) {

        PlayerProfile existing =
                profiles.get(uniqueId);

        if (existing != null) {
            return existing;
        }

        Account account =
                accountManager.get(uniqueId);

        if (account == null) {
            return null;
        }

        PlayerProfile profile =
                new PlayerProfile(
                        account,
                        statisticsRepository
                );

        profiles.put(
                uniqueId,
                profile
        );

        return profile;
    }

    public PlayerProfile load(
            UUID uniqueId
    ) {

        Account account =
                accountManager.get(
                        uniqueId
                );

        if (account == null) {
            return null;
        }

        PlayerProfile profile =
                new PlayerProfile(
                        account,
                        statisticsRepository
                );

        profiles.put(
                uniqueId,
                profile
        );

        return profile;
    }

    public void save(
            PlayerProfile profile
    ) {

        accountManager.save(
                profile.getAccount()
        );

        profile.saveAllStatistics();
    }

    public void unload(
            UUID uniqueId
    ) {

        PlayerProfile profile =
                profiles.remove(
                        uniqueId
                );

        if (profile == null) {
            return;
        }

        save(profile);
    }

    public void clear() {

        profiles.clear();
    }
}