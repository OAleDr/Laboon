package br.com.laboon.core.progression.prestige;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.database.DatabaseManager;
import br.com.laboon.core.progression.LevelCalculator;
import br.com.laboon.core.progression.ProgressionGame;
import br.com.laboon.core.progression.ProgressionService;
import br.com.laboon.core.progression.ProgressionSnapshot;
import br.com.laboon.core.progression.prestige.PrestigeResult;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PrestigeService {

    private final DatabaseManager database;
    private final AccountManager accountManager;
    private final PrestigeRepository repository;
    private final PrestigeTransactionRepository transactionRepository;
    private final ProgressionService progressionService;
    private final LevelCalculator globalCalculator;
    private final int maxPrestige;

    private final List<PrestigeListener> listeners =
            new CopyOnWriteArrayList<>();

    public PrestigeService(
            DatabaseManager database,
            AccountManager accountManager,
            PrestigeRepository repository,
            PrestigeTransactionRepository transactionRepository,
            ProgressionService progressionService
    ) {
        this(
                database,
                accountManager,
                repository,
                transactionRepository,
                progressionService,
                100
        );
    }

    public PrestigeService(
            DatabaseManager database,
            AccountManager accountManager,
            PrestigeRepository repository,
            PrestigeTransactionRepository transactionRepository,
            ProgressionService progressionService,
            int maxPrestige
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
                    "PrestigeRepository não pode ser nulo."
            );
        }

        if (transactionRepository == null) {
            throw new IllegalArgumentException(
                    "PrestigeTransactionRepository não pode ser nulo."
            );
        }

        if (progressionService == null) {
            throw new IllegalArgumentException(
                    "ProgressionService não pode ser nulo."
            );
        }

        if (maxPrestige < 1) {
            throw new IllegalArgumentException(
                    "maxPrestige deve ser maior que zero."
            );
        }

        this.database = database;
        this.accountManager = accountManager;
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.progressionService = progressionService;
        this.globalCalculator =
                LevelCalculator.defaultCalculator();
        this.maxPrestige = maxPrestige;
    }

    public int getPrestige(UUID playerUuid) {
        validatePlayer(playerUuid);
        return repository.getPrestige(playerUuid);
    }

    public Prestige get(UUID playerUuid) {
        validatePlayer(playerUuid);

        return new Prestige(
                playerUuid,
                getPrestige(playerUuid)
        );
    }

    public int getMaxPrestige() {
        return maxPrestige;
    }

    public PrestigeDefinition definition(int prestige) {
        if (prestige < 1) {
            throw new IllegalArgumentException(
                    "Prestige alvo deve ser maior que zero."
            );
        }

        return new PrestigeDefinition(
                prestige,
                globalCalculator.getMaxLevel()
        );
    }

    public void addListener(PrestigeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(PrestigeListener listener) {
        listeners.remove(listener);
    }

    public PrestigeResult prestige(UUID playerUuid) {
        if (playerUuid == null) {
            return PrestigeResult.failure(
                    PrestigeResult.Status.INVALID_PLAYER
            );
        }

        Account account =
                accountManager.get(playerUuid);

        if (account == null) {
            return PrestigeResult.failure(
                    PrestigeResult.Status.FAILED
            );
        }

        int currentPrestige =
                repository.getPrestige(playerUuid);

        if (currentPrestige >= maxPrestige) {
            return PrestigeResult.failure(
                    PrestigeResult.Status.MAX_PRESTIGE
            );
        }

        int level =
                progressionService.getLevel(playerUuid);

        if (level < globalCalculator.getMaxLevel()) {
            return PrestigeResult.failure(
                    PrestigeResult.Status.REQUIREMENT_NOT_MET
            );
        }

        PromotionResult promotion =
                promoteTransactionally(playerUuid);

        if (!promotion.success()) {
            return PrestigeResult.failure(
                    promotion.status()
            );
        }

        ProgressionSnapshot snapshot =
                progressionService.getSnapshot(
                        playerUuid,
                        ProgressionGame.GLOBAL
                );

        PrestigeLevelUpEvent event =
                new PrestigeLevelUpEvent(
                        playerUuid,
                        promotion.previousPrestige(),
                        promotion.newPrestige()
                );

        for (PrestigeListener listener : listeners) {
            listener.onPrestige(event);
        }

        return PrestigeResult.success(
                new Prestige(
                        playerUuid,
                        promotion.newPrestige()
                ),
                snapshot
        );
    }

    private PromotionResult promoteTransactionally(
            UUID playerUuid
    ) {
        try (
                Connection connection =
                        database.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                /*
                 * IMPORTANT:
                 * O prestige é lido dentro da mesma transação
                 * e com FOR UPDATE no PostgreSQL.
                 */
                int currentPrestige =
                        transactionRepository.getPrestige(
                                connection,
                                playerUuid
                        );

                if (currentPrestige >= maxPrestige) {
                    connection.rollback();

                    return new PromotionResult(
                            false,
                            currentPrestige,
                            currentPrestige,
                            PrestigeResult.Status.MAX_PRESTIGE
                    );
                }

                /*
                 * O requisito de nível também é validado novamente
                 * usando a conta atualmente carregada.
                 */
                Account account =
                        accountManager.get(playerUuid);

                if (account == null) {
                    connection.rollback();

                    return new PromotionResult(
                            false,
                            currentPrestige,
                            currentPrestige,
                            PrestigeResult.Status.FAILED
                    );
                }

                int currentLevel =
                        globalCalculator.levelFromExperience(
                                account.getExperience()
                        );

                if (
                        currentLevel <
                                globalCalculator.getMaxLevel()
                ) {
                    connection.rollback();

                    return new PromotionResult(
                            false,
                            currentPrestige,
                            currentPrestige,
                            PrestigeResult.Status.REQUIREMENT_NOT_MET
                    );
                }

                int newPrestige =
                        currentPrestige + 1;

                transactionRepository.setPrestige(
                        connection,
                        playerUuid,
                        newPrestige
                );

                /*
                 * Reset do XP global na mesma transação.
                 */
                String sql = """
                        UPDATE "accounts"
                        SET "experience" = 0
                        WHERE "uniqueId" = ?
                        """;

                try (
                        var statement =
                                connection.prepareStatement(sql)
                ) {
                    statement.setObject(
                            1,
                            playerUuid
                    );

                    int affected =
                            statement.executeUpdate();

                    if (affected != 1) {
                        throw new IllegalStateException(
                                "Account não encontrada: "
                                        + playerUuid
                        );
                    }
                }

                connection.commit();

                /*
                 * Somente depois do commit atualizamos o cache.
                 */
                Account cached =
                        accountManager.get(playerUuid);

                if (cached != null) {
                    cached.setExperience(0L);
                }

                return new PromotionResult(
                        true,
                        currentPrestige,
                        newPrestige,
                        PrestigeResult.Status.SUCCESS
                );

            } catch (Exception exception) {

                try {
                    connection.rollback();
                } catch (Exception rollbackException) {
                    exception.addSuppressed(
                            rollbackException
                    );
                }

                return new PromotionResult(
                        false,
                        0,
                        0,
                        PrestigeResult.Status.FAILED
                );
            }

        } catch (Exception exception) {

            return new PromotionResult(
                    false,
                    0,
                    0,
                    PrestigeResult.Status.FAILED
            );
        }
    }

    private void validatePlayer(UUID playerUuid) {
        if (playerUuid == null) {
            throw new IllegalArgumentException(
                    "UUID do player não pode ser nulo."
            );
        }
    }

    private record PromotionResult(
            boolean success,
            int previousPrestige,
            int newPrestige,
            PrestigeResult.Status status
    ) {
    }
}