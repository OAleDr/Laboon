package br.com.laboon.bukkit.profile;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountRepository;
import br.com.laboon.core.profile.GameCoinsRepository;
import br.com.laboon.core.profile.PlayerProfile;
import br.com.laboon.core.profile.StatisticsRepository;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BukkitProfileProvider implements ProfileProvider {

    private final AccountRepository accountRepository;

    private final StatisticsRepository statisticsRepository;

    private final GameCoinsRepository gameCoinsRepository;

    private final ConcurrentMap<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

    public BukkitProfileProvider(AccountRepository accountRepository, StatisticsRepository statisticsRepository, GameCoinsRepository gameCoinsRepository) {

        this.accountRepository = accountRepository;

        this.statisticsRepository = statisticsRepository;

        this.gameCoinsRepository = gameCoinsRepository;
    }

    @Override
    public PlayerProfile getProfile(Player player) {

        return get(player.getUniqueId());
    }

    public PlayerProfile get(UUID uniqueId) {

        PlayerProfile existing = profiles.get(uniqueId);

        if (existing != null) {
            return existing;
        }

        Account account = accountRepository.findById(uniqueId);

        if (account == null) {
            return null;
        }

        PlayerProfile profile = new PlayerProfile(account, statisticsRepository, gameCoinsRepository);

        profiles.put(uniqueId, profile);

        return profile;
    }

    @Override
    public void save(PlayerProfile profile) {

        accountRepository.save(profile.getAccount());

        profile.saveAll();
    }

    @Override
    public void unload(UUID uniqueId) {
        profiles.remove(uniqueId);
    }

    @Override
    public void saveAll() {

        for (PlayerProfile profile : profiles.values()) {

            save(profile);
        }
    }

}