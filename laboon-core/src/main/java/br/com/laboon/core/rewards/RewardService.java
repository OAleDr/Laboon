package br.com.laboon.core.rewards;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.database.DatabaseManager;
import br.com.laboon.core.economy.EconomyCurrency;
import br.com.laboon.core.economy.EconomyTransaction;
import br.com.laboon.core.economy.EconomyTransactionType;

import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class RewardService {

    private final DatabaseManager database;
    private final AccountManager accountManager;
    private final RewardTransactionRepository repository;

    public RewardService(
            DatabaseManager database,
            AccountManager accountManager,
            RewardTransactionRepository repository
    ) {

        if (database == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager não pode ser nulo."
            );
        }

        if (accountManager == null) {
            throw new IllegalArgumentException(
                    "AccountManager não pode ser nulo."
            );
        }

        if (repository == null) {
            throw new IllegalArgumentException(
                    "RewardTransactionRepository não pode ser nulo."
            );
        }

        this.database = database;
        this.accountManager = accountManager;
        this.repository = repository;
    }

    public RewardResult give(
            UUID playerUuid,
            Reward reward,
            RewardSource source
    ) {

        return execute(
                playerUuid,
                reward,
                source,
                null
        );
    }

    public RewardResult give(
            UUID playerUuid,
            Reward reward,
            RewardSource source,
            String rewardId
    ) {

        return execute(
                playerUuid,
                reward,
                source,
                rewardId
        );
    }

    public RewardResult giveCoins(
            UUID playerUuid,
            long amount,
            RewardSource source
    ) {

        return give(
                playerUuid,
                new Reward()
                        .addCoins(amount),
                source
        );
    }

    public RewardResult giveCoins(
            UUID playerUuid,
            long amount,
            RewardSource source,
            String rewardId
    ) {

        return give(
                playerUuid,
                new Reward()
                        .addCoins(amount),
                source,
                rewardId
        );
    }

    public RewardResult giveTokens(
            UUID playerUuid,
            long amount,
            RewardSource source
    ) {

        return give(
                playerUuid,
                new Reward()
                        .addTokens(amount),
                source
        );
    }

    public RewardResult giveTokens(
            UUID playerUuid,
            long amount,
            RewardSource source,
            String rewardId
    ) {

        return give(
                playerUuid,
                new Reward()
                        .addTokens(amount),
                source,
                rewardId
        );
    }

    public RewardResult giveExperience(
            UUID playerUuid,
            long amount,
            RewardSource source
    ) {

        return give(
                playerUuid,
                new Reward()
                        .addExperience(amount),
                source
        );
    }

    public RewardResult giveExperience(
            UUID playerUuid,
            long amount,
            RewardSource source,
            String rewardId
    ) {

        return give(
                playerUuid,
                new Reward()
                        .addExperience(amount),
                source,
                rewardId
        );
    }

    private RewardResult execute(
            UUID playerUuid,
            Reward reward,
            RewardSource source,
            String rewardId
    ) {

        if (playerUuid == null) {

            return RewardResult.failure(
                    RewardResult.Status.INVALID_PLAYER
            );
        }

        if (reward == null || reward.isEmpty()) {

            return RewardResult.failure(
                    RewardResult.Status.EMPTY_REWARD
            );
        }

        if (
                rewardId != null
                        && (
                        rewardId.isBlank()
                                || rewardId.length() > 128
                )
        ) {

            return RewardResult.failure(
                    RewardResult.Status.INVALID_REWARD_ID
            );
        }

        RewardSource safeSource =
                source == null
                        ? RewardSource.OTHER
                        : source;

        try (
                Connection connection =
                        database.getConnection()
        ) {

            connection.setAutoCommit(false);

            try {

                /*
                 * CLAIM
                 */

                if (rewardId != null) {

                    boolean claimed =
                            repository.tryClaim(
                                    connection,
                                    rewardId,
                                    playerUuid,
                                    safeSource
                            );

                    if (!claimed) {

                        connection.rollback();

                        return RewardResult.alreadyClaimed();
                    }
                }

                /*
                 * ACCOUNT
                 */

                Account account =
                        repository.findAccount(
                                connection,
                                playerUuid
                        );

                if (account == null) {

                    throw new IllegalStateException(
                            "Account não encontrada: "
                                    + playerUuid
                    );
                }

                boolean containsExperience =
                        false;

                /*
                 * REWARDS
                 */

                for (
                        Reward.RewardEntry entry :
                        reward.getEntries()
                ) {

                    switch (entry.getType()) {

                        case COINS:

                            applyEconomy(
                                    connection,
                                    playerUuid,
                                    EconomyCurrency.COINS,
                                    entry.getAmount(),
                                    safeSource
                            );

                            break;

                        case TOKENS:

                            applyEconomy(
                                    connection,
                                    playerUuid,
                                    EconomyCurrency.TOKENS,
                                    entry.getAmount(),
                                    safeSource
                            );

                            break;

                        case EXPERIENCE:

                            containsExperience = true;

                            applyExperience(
                                    account,
                                    entry.getAmount()
                            );

                            break;

                        case COSMETIC:
                            break;

                        case TITLE:
                            break;

                        case ITEM:
                            break;
                    }
                }

                /*
                 * XP NA MESMA TRANSAÇÃO
                 */

                if (containsExperience) {

                    repository.saveAccount(
                            connection,
                            account
                    );
                }

                /*
                 * COMMIT
                 */

                connection.commit();

                /*
                 * CACHE SOMENTE DEPOIS DO COMMIT
                 */

                if (containsExperience) {

                    Account cached =
                            accountManager.get(
                                    playerUuid
                            );

                    if (cached != null) {

                        cached.setExperience(
                                account.getExperience()
                        );
                    }
                }

                return RewardResult.success(
                        List.of(reward)
                );

            } catch (Exception exception) {

                try {
                    connection.rollback();
                } catch (Exception rollbackException) {
                    exception.addSuppressed(
                            rollbackException
                    );
                }

                throw exception;
            }

        } catch (Exception exception) {

            return RewardResult.failure(
                    RewardResult.Status.FAILED
            );
        }
    }

    private void applyEconomy(
            Connection connection,
            UUID playerUuid,
            EconomyCurrency currency,
            long amount,
            RewardSource source
    ) {

        if (amount <= 0L) {

            throw new IllegalArgumentException(
                    "Quantidade da recompensa deve ser maior que zero."
            );
        }

        repository.updateEconomy(
                connection,
                playerUuid,
                currency,
                amount
        );

        repository.saveEconomyTransaction(
                connection,
                new EconomyTransaction(
                        UUID.randomUUID(),
                        playerUuid,
                        currency,
                        amount,
                        EconomyTransactionType.REWARD,
                        "reward:"
                                + source
                                .name()
                                .toLowerCase(),
                        null,
                        Instant.now()
                )
        );
    }

    private void applyExperience(
            Account account,
            long amount
    ) {

        if (amount <= 0L) {

            throw new IllegalArgumentException(
                    "Experience deve ser maior que zero."
            );
        }

        long current =
                account.getExperience();

        long next;

        try {

            next =
                    Math.addExact(
                            current,
                            amount
                    );

        } catch (ArithmeticException exception) {

            next =
                    Long.MAX_VALUE;
        }

        account.setExperience(
                next
        );
    }
}