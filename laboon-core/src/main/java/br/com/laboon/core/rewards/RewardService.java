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
    private final RewardClaimRepository claimRepository;

    public RewardService(
            EconomyService economyService,
            AccountManager accountManager
    ) {

        this(
                economyService,
                accountManager,
                null
        );
    }

    public RewardService(
            EconomyService economyService,
            AccountManager accountManager,
            RewardClaimRepository claimRepository
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
        this.claimRepository = claimRepository;
    }

    /**
     * API original.
     */
    public RewardResult give(
            UUID playerUuid,
            Reward reward,
            RewardSource source
    ) {

        return applyReward(
                playerUuid,
                reward,
                source
        );
    }

    /**
     * API idempotente.
     */
    public RewardResult give(
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
                rewardId == null
                        || rewardId.isBlank()
                        || rewardId.length() > 128
        ) {

            return RewardResult.failure(
                    RewardResult.Status.INVALID_REWARD_ID
            );
        }

        if (claimRepository == null) {

            throw new IllegalStateException(
                    "RewardClaimRepository não foi configurado."
            );
        }

        RewardSource safeSource =
                source == null
                        ? RewardSource.OTHER
                        : source;

        boolean claimed =
                claimRepository.tryClaim(
                        rewardId,
                        playerUuid,
                        safeSource
                );

        if (!claimed) {

            return RewardResult.alreadyClaimed();
        }

        try {

            return applyReward(
                    playerUuid,
                    reward,
                    safeSource
            );

        } catch (RuntimeException exception) {

            /*
             * Atenção:
             *
             * Nesta arquitetura atual o claim já foi gravado
             * antes da aplicação da recompensa.
             *
             * Por isso esta API ainda não deve ser considerada
             * atomicamente transacional entre claim + XP + economy.
             *
             * A etapa seguinte será mover o claim para a mesma
             * Connection usada pela transação da recompensa.
             */
            throw exception;
        }
    }

    private RewardResult applyReward(
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

        RewardSource safeSource =
                source == null
                        ? RewardSource.OTHER
                        : source;

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
                            safeSource
                    );

                    break;

                case TOKENS:

                    applyEconomyReward(
                            playerUuid,
                            EconomyCurrency.TOKENS,
                            entry.getAmount(),
                            safeSource
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
                     * Futuro sistema de Cosmetics.
                     */
                    break;

                case TITLE:

                    /*
                     * Futuro sistema de Identity.
                     */
                    break;

                case ITEM:

                    /*
                     * Futuramente tratado no Bukkit.
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
                        "reward:"
                                + source
                                .name()
                                .toLowerCase(),
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
                accountManager.get(
                        playerUuid
                );

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