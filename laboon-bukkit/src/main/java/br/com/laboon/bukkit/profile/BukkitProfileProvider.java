package br.com.laboon.bukkit.profile;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountRepository;
import br.com.laboon.core.profile.PlayerProfile;
import br.com.laboon.core.profile.StatisticsRepository;

import org.bukkit.entity.Player;

public final class BukkitProfileProvider implements ProfileProvider {

    private final AccountRepository accountRepository;

    private final StatisticsRepository statisticsRepository;

    public BukkitProfileProvider(AccountRepository accountRepository, StatisticsRepository statisticsRepository) {

        this.accountRepository = accountRepository;

        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public PlayerProfile getProfile(Player player) {

        Account account = accountRepository.findById(player.getUniqueId());

        if (account == null) {

            return null;
        }

        return new PlayerProfile(account, statisticsRepository);
    }
}