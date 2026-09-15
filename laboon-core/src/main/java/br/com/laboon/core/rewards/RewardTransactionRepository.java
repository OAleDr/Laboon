package br.com.laboon.core.rewards;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.economy.EconomyCurrency;
import br.com.laboon.core.economy.EconomyTransaction;

import java.sql.Connection;
import java.util.UUID;

public interface RewardTransactionRepository {

    boolean tryClaim(
            Connection connection,
            String rewardId,
            UUID playerUuid,
            RewardSource source
    );

    Account findAccount(
            Connection connection,
            UUID playerUuid
    );

    void saveAccount(
            Connection connection,
            Account account
    );

    void updateEconomy(
            Connection connection,
            UUID playerUuid,
            EconomyCurrency currency,
            long amount
    );

    void saveEconomyTransaction(
            Connection connection,
            EconomyTransaction transaction
    );
}