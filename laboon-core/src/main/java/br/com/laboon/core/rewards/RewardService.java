package br.com.laboon.core.rewards;

import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.economy.EconomyCurrency;
import br.com.laboon.core.economy.EconomyResult;
import br.com.laboon.core.economy.EconomyService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RewardService {

    private final EconomyService economyService;
    private final AccountManager accountManager;

    public RewardService(
            EconomyService economyService,
            AccountManager accountManager
    ) {

        if (economyService == null) {
            throw new IllegalArgumentException(
                    "EconomyService não pode ser nulo."
            );
        }

        if (accountManager == null) {
            throw new IllegalArgumentException(
                    "AccountManager não pode ser nulo."
            );
        }

        this.economyService = economyService;
        this.accountManager = accountManager;
    }

    public RewardResult give(
            UUID playerUuid,
            Reward reward,
            RewardSource source
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

        if (source == null) {
            source = RewardSource.OTHER;
        }

        List<Reward> applied =
                new ArrayList<>();

        for (
                Reward.RewardEntry entry :
                reward.getEntries()
        ) {

            switch (entry.getType()) {

                case COINS:

                    applyEconomyReward(
                            playerUuid,
                            EconomyCurrency.COINS,
                            entry.getAmount(),
                            source
                    );

                    break;

                case TOKENS:

                    applyEconomyReward(
                            playerUuid,
                            EconomyCurrency.TOKENS,
                            entry.getAmount(),
                            source
                    );

                    break;

                case EXPERIENCE:

                    applyExperience(
                            playerUuid,
                            entry.getAmount()
                    );

                    break;

                case COSMETIC:
                    /*
                     * Será implementado quando
                     * o sistema de Cosmetics existir.
                     */
                    break;

                case TITLE:
                    /*
                     * Será implementado quando
                     * o sistema de Identity existir.
                     */
                    break;

                case ITEM:
                    /*
                     * Item será responsabilidade
                     * do Bukkit futuramente.
                     */
                    break;
            }
        }

        applied.add(reward);

        return RewardResult.success(
                applied
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

    private void applyEconomyReward(
            UUID playerUuid,
            EconomyCurrency currency,
            long amount,
            RewardSource source
    ) {

        EconomyResult result =
                economyService.reward(
                        playerUuid,
                        currency,
                        amount,
                        "reward:" + source.name().toLowerCase(),
                        null
                );

        if (!result.isSuccess()) {

            throw new IllegalStateException(
                    "Não foi possível entregar recompensa de "
                            + currency
                            + " para "
                            + playerUuid
                            + ". Status: "
                            + result.getStatus()
            );
        }
    }

    private void applyExperience(
            UUID playerUuid,
            long amount
    ) {

        var account =
                accountManager.get(playerUuid);

        if (account == null) {

            throw new IllegalStateException(
                    "Account não encontrada para "
                            + playerUuid
            );
        }

        long current =
                account.getExperience();

        long next;

        try {

            next = Math.addExact(
                    current,
                    amount
            );

        } catch (ArithmeticException exception) {

            next = Long.MAX_VALUE;
        }

        account.setExperience(
                next
        );

        accountManager.save(
                account
        );
    }
}