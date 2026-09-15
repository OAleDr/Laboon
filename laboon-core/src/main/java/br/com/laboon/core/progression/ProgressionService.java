package br.com.laboon.core.progression;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ProgressionService {

    private final AccountManager accountManager;
    private final ProgressionRepository repository;
    private final LevelCalculator globalCalculator;
    private final LevelCalculator gameCalculator;

    private final List<ProgressionLevelUpListener> listeners =
            new CopyOnWriteArrayList<>();

    public ProgressionService(
            AccountManager accountManager,
            ProgressionRepository repository
    ) {
        this(
                accountManager,
                repository,
                LevelCalculator.defaultCalculator(),
                LevelCalculator.defaultCalculator()
        );
    }

    public ProgressionService(
            AccountManager accountManager,
            ProgressionRepository repository,
            LevelCalculator globalCalculator
    ) {
        this(
                accountManager,
                repository,
                globalCalculator,
                globalCalculator
        );
    }

    public ProgressionService(
            AccountManager accountManager,
            ProgressionRepository repository,
            LevelCalculator globalCalculator,
            LevelCalculator gameCalculator
    ) {
        if (accountManager == null) {
            throw new IllegalArgumentException(
                    "AccountManager não pode ser nulo."
            );
        }

        if (repository == null) {
            throw new IllegalArgumentException(
                    "ProgressionRepository não pode ser nulo."
            );
        }

        if (globalCalculator == null) {
            throw new IllegalArgumentException(
                    "Global LevelCalculator não pode ser nulo."
            );
        }

        if (gameCalculator == null) {
            throw new IllegalArgumentException(
                    "Game LevelCalculator não pode ser nulo."
            );
        }

        this.accountManager = accountManager;
        this.repository = repository;
        this.globalCalculator = globalCalculator;
        this.gameCalculator = gameCalculator;
    }

    public void addLevelUpListener(
            ProgressionLevelUpListener listener
    ) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeLevelUpListener(
            ProgressionLevelUpListener listener
    ) {
        listeners.remove(listener);
    }

    public long getExperience(
            UUID playerUuid,
            ProgressionGame game
    ) {
        validatePlayer(playerUuid);

        if (game == null) {
            throw new IllegalArgumentException(
                    "ProgressionGame não pode ser nulo."
            );
        }

        if (game == ProgressionGame.GLOBAL) {
            return requireAccount(
                    playerUuid
            ).getExperience();
        }

        return repository.getExperience(
                playerUuid,
                game
        );
    }

    public int getLevel(
            UUID playerUuid
    ) {
        return globalCalculator.levelFromExperience(
                getExperience(
                        playerUuid,
                        ProgressionGame.GLOBAL
                )
        );
    }

    public int getLevel(
            UUID playerUuid,
            ProgressionGame game
    ) {
        return calculatorFor(game)
                .levelFromExperience(
                        getExperience(
                                playerUuid,
                                game
                        )
                );
    }

    public ProgressionSnapshot getSnapshot(
            UUID playerUuid
    ) {
        return getSnapshot(
                playerUuid,
                ProgressionGame.GLOBAL
        );
    }

    public ProgressionSnapshot getSnapshot(
            UUID playerUuid,
            ProgressionGame game
    ) {
        long experience =
                getExperience(
                        playerUuid,
                        game
                );

        LevelCalculator calculator =
                calculatorFor(game);

        int level =
                calculator.levelFromExperience(
                        experience
                );

        return createSnapshot(
                playerUuid,
                game,
                experience,
                level,
                level
        );
    }

    public ProgressionResult addExperience(
            UUID playerUuid,
            long amount,
            ExperienceSource source
    ) {
        return addExperience(
                playerUuid,
                amount,
                ProgressionGame.GLOBAL,
                source
        );
    }

    public ProgressionResult addExperience(
            UUID playerUuid,
            long amount,
            ProgressionGame game,
            ExperienceSource source
    ) {
        if (playerUuid == null) {
            return ProgressionResult.failure(
                    ProgressionResult.Status.INVALID_PLAYER
            );
        }

        if (game == null) {
            return ProgressionResult.failure(
                    ProgressionResult.Status.INVALID_GAME
            );
        }

        if (amount <= 0L) {
            return ProgressionResult.failure(
                    ProgressionResult.Status.INVALID_AMOUNT
            );
        }

        Account account =
                requireAccount(playerUuid);

        LevelCalculator calculator =
                calculatorFor(game);

        long oldExperience =
                game == ProgressionGame.GLOBAL
                        ? account.getExperience()
                        : repository.getExperience(
                                playerUuid,
                                game
                        );

        int oldLevel =
                calculator.levelFromExperience(
                        oldExperience
                );

        long newExperience;

        try {
            newExperience =
                    Math.addExact(
                            oldExperience,
                            amount
                    );
        } catch (ArithmeticException exception) {
            newExperience =
                    Long.MAX_VALUE;
        }

        if (game == ProgressionGame.GLOBAL) {

            account.setExperience(
                    newExperience
            );

            accountManager.save(
                    account
            );

        } else {

            repository.setExperience(
                    playerUuid,
                    game,
                    newExperience
            );
        }

        int newLevel =
                calculator.levelFromExperience(
                        newExperience
                );

        List<Integer> levelsGained =
                levelsGained(
                        oldLevel,
                        newLevel
                );

        ProgressionSnapshot snapshot =
                createSnapshot(
                        playerUuid,
                        game,
                        newExperience,
                        newLevel,
                        oldLevel
                );

        try {
            repository.saveHistory(
                    new ProgressionXpEntry(
                            UUID.randomUUID(),
                            playerUuid,
                            game,
                            amount,
                            source == null
                                    ? ExperienceSource.OTHER
                                    : source,
                            null,
                            Instant.now()
                    )
            );
        } catch (RuntimeException ignored) {
            /*
             * Histórico não deve impedir o XP já salvo.
             */
        }

        for (int level : levelsGained) {

            for (
                    ProgressionLevelUpListener listener :
                    listeners
            ) {

                listener.onLevelUp(
                        snapshot,
                        level
                );
            }
        }

        if (
                newExperience ==
                        oldExperience
        ) {

            return ProgressionResult.noChange(
                    snapshot
            );
        }

        return ProgressionResult.success(
                snapshot,
                levelsGained
        );
    }

    public ProgressionResult setExperience(
            UUID playerUuid,
            long experience,
            ProgressionGame game
    ) {
        if (playerUuid == null) {
            return ProgressionResult.failure(
                    ProgressionResult.Status.INVALID_PLAYER
            );
        }

        if (game == null) {
            return ProgressionResult.failure(
                    ProgressionResult.Status.INVALID_GAME
            );
        }

        if (experience < 0L) {
            return ProgressionResult.failure(
                    ProgressionResult.Status.INVALID_AMOUNT
            );
        }

        Account account =
                requireAccount(playerUuid);

        LevelCalculator calculator =
                calculatorFor(game);

        long oldExperience =
                game == ProgressionGame.GLOBAL
                        ? account.getExperience()
                        : repository.getExperience(
                                playerUuid,
                                game
                        );

        int oldLevel =
                calculator.levelFromExperience(
                        oldExperience
                );

        if (game == ProgressionGame.GLOBAL) {

            account.setExperience(
                    experience
            );

            accountManager.save(
                    account
            );

        } else {

            repository.setExperience(
                    playerUuid,
                    game,
                    experience
            );
        }

        int newLevel =
                calculator.levelFromExperience(
                        experience
                );

        List<Integer> levelsGained =
                newLevel > oldLevel
                        ? levelsGained(
                                oldLevel,
                                newLevel
                        )
                        : List.of();

        ProgressionSnapshot snapshot =
                createSnapshot(
                        playerUuid,
                        game,
                        experience,
                        newLevel,
                        oldLevel
                );

        return ProgressionResult.success(
                snapshot,
                levelsGained
        );
    }

    public List<ProgressionXpEntry> getHistory(
            UUID playerUuid,
            ProgressionGame game,
            int limit
    ) {
        validatePlayer(playerUuid);

        if (game == null) {
            throw new IllegalArgumentException(
                    "ProgressionGame não pode ser nulo."
            );
        }

        if (limit <= 0) {
            return List.of();
        }

        return repository.getHistory(
                playerUuid,
                game,
                limit
        );
    }

    private LevelCalculator calculatorFor(
            ProgressionGame game
    ) {
        if (game == null) {
            throw new IllegalArgumentException(
                    "ProgressionGame não pode ser nulo."
            );
        }

        return game == ProgressionGame.GLOBAL
                ? globalCalculator
                : gameCalculator;
    }

    private List<Integer> levelsGained(
            int oldLevel,
            int newLevel
    ) {
        List<Integer> result =
                new ArrayList<>();

        for (
                int level = oldLevel + 1;
                level <= newLevel;
                level++
        ) {
            result.add(level);
        }

        return result;
    }

    private ProgressionSnapshot createSnapshot(
            UUID playerUuid,
            ProgressionGame game,
            long experience,
            int level,
            int previousLevel
    ) {
        long currentLevelXp =
                calculatorFor(game)
                        .requiredExperience(
                                level
                        );

        long nextLevelXp =
                calculatorFor(game)
                        .requiredExperienceForNextLevel(
                                level
                        );

        long intoLevel =
                experience >= currentLevelXp
                        ? experience - currentLevelXp
                        : 0L;

        return new ProgressionSnapshot(
                playerUuid,
                game,
                experience,
                level,
                previousLevel,
                level > previousLevel,
                intoLevel,
                nextLevelXp
        );
    }

    private Account requireAccount(
            UUID playerUuid
    ) {
        Account account =
                accountManager.get(
                        playerUuid
                );

        if (account == null) {
            throw new IllegalStateException(
                    "Account não encontrada: "
                            + playerUuid
            );
        }

        return account;
    }

    private void validatePlayer(
            UUID playerUuid
    ) {
        if (playerUuid == null) {
            throw new IllegalArgumentException(
                    "UUID do player não pode ser nulo."
            );
        }
    }
}
