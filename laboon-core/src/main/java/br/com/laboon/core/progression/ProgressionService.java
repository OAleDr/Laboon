package br.com.laboon.core.progression;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProgressionService {

    private final AccountManager accountManager;
    private final ProgressionRepository repository;
    private final LevelCalculator globalCalculator;

    public ProgressionService(
            AccountManager accountManager,
            ProgressionRepository repository
    ) {
        this(accountManager, repository, LevelCalculator.defaultCalculator());
    }

    public ProgressionService(
            AccountManager accountManager,
            ProgressionRepository repository,
            LevelCalculator globalCalculator
    ) {
        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }
        if (repository == null) {
            throw new IllegalArgumentException("ProgressionRepository não pode ser nulo.");
        }
        if (globalCalculator == null) {
            throw new IllegalArgumentException("LevelCalculator não pode ser nulo.");
        }
        this.accountManager = accountManager;
        this.repository = repository;
        this.globalCalculator = globalCalculator;
    }

    public long getExperience(UUID playerUuid, ProgressionGame game) {
        validatePlayer(playerUuid);
        validateGame(game);
        if (game == ProgressionGame.GLOBAL) {
            Account account = requireAccount(playerUuid);
            return account.getExperience();
        }
        return repository.getExperience(playerUuid, game);
    }

    public int getLevel(UUID playerUuid) {
        long experience = getExperience(playerUuid, ProgressionGame.GLOBAL);
        return globalCalculator.levelFromExperience(experience);
    }

    public ProgressionSnapshot getSnapshot(UUID playerUuid) {
        return snapshot(playerUuid, ProgressionGame.GLOBAL, getExperience(playerUuid, ProgressionGame.GLOBAL));
    }

    public ProgressionResult addExperience(
            UUID playerUuid,
            long amount,
            ExperienceSource source
    ) {
        return addExperience(playerUuid, amount, ProgressionGame.GLOBAL, source);
    }

    public ProgressionResult addExperience(
            UUID playerUuid,
            long amount,
            ProgressionGame game,
            ExperienceSource source
    ) {
        validatePlayer(playerUuid);
        validateGame(game);
        if (amount <= 0L) {
            return ProgressionResult.failure(ProgressionResult.Status.INVALID_AMOUNT);
        }

        Account account = requireAccount(playerUuid);

        long oldExperience = game == ProgressionGame.GLOBAL
                ? account.getExperience()
                : repository.getExperience(playerUuid, game);

        LevelCalculator calculator = calculatorFor(game);
        int oldLevel = calculator.levelFromExperience(oldExperience);

        long newExperience;
        try {
            newExperience = Math.addExact(oldExperience, amount);
        } catch (ArithmeticException exception) {
            newExperience = Long.MAX_VALUE;
        }

        if (game == ProgressionGame.GLOBAL) {
            account.setExperience(newExperience);
            accountManager.save(account);
        } else {
            repository.setExperience(playerUuid, game, newExperience);
        }

        int newLevel = calculator.levelFromExperience(newExperience);
        List<Integer> levelsGained = new ArrayList<>();
        for (int level = oldLevel + 1; level <= newLevel; level++) {
            levelsGained.add(level);
        }

        ProgressionSnapshot snapshot = snapshot(
                playerUuid,
                game,
                newExperience,
                calculator,
                oldLevel
        );

        if (newExperience == oldExperience) {
            return ProgressionResult.noChange(snapshot);
        }

        return ProgressionResult.success(snapshot, levelsGained);
    }

    public ProgressionResult setExperience(
            UUID playerUuid,
            long experience,
            ProgressionGame game
    ) {
        validatePlayer(playerUuid);
        validateGame(game);
        if (experience < 0L) {
            return ProgressionResult.failure(ProgressionResult.Status.INVALID_AMOUNT);
        }

        Account account = requireAccount(playerUuid);
        long oldExperience = game == ProgressionGame.GLOBAL
                ? account.getExperience()
                : repository.getExperience(playerUuid, game);

        LevelCalculator calculator = calculatorFor(game);
        int oldLevel = calculator.levelFromExperience(oldExperience);

        if (game == ProgressionGame.GLOBAL) {
            account.setExperience(experience);
            accountManager.save(account);
        } else {
            repository.setExperience(playerUuid, game, experience);
        }

        int newLevel = calculator.levelFromExperience(experience);
        List<Integer> levelsGained = new ArrayList<>();
        if (newLevel > oldLevel) {
            for (int level = oldLevel + 1; level <= newLevel; level++) {
                levelsGained.add(level);
            }
        }

        return ProgressionResult.success(
                snapshot(playerUuid, game, experience, calculator, oldLevel),
                levelsGained
        );
    }

    private ProgressionSnapshot snapshot(
            UUID playerUuid,
            ProgressionGame game,
            long experience
    ) {
        return snapshot(
                playerUuid,
                game,
                experience,
                calculatorFor(game),
                calculatorFor(game).levelFromExperience(experience)
        );
    }

    private ProgressionSnapshot snapshot(
            UUID playerUuid,
            ProgressionGame game,
            long experience,
            LevelCalculator calculator,
            int previousLevel
    ) {
        int level = calculator.levelFromExperience(experience);
        long currentLevelXp = calculator.requiredExperience(level);
        long nextLevelXp = calculator.requiredExperienceForNextLevel(level);
        long intoLevel = experience >= currentLevelXp
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

    private LevelCalculator calculatorFor(ProgressionGame game) {
        return globalCalculator;
    }

    private Account requireAccount(UUID playerUuid) {
        Account account = accountManager.get(playerUuid);
        if (account == null) {
            throw new IllegalStateException(
                    "Account não encontrada: " + playerUuid
            );
        }
        return account;
    }

    private void validatePlayer(UUID playerUuid) {
        if (playerUuid == null) {
            throw new IllegalArgumentException("UUID do player não pode ser nulo.");
        }
    }

    private void validateGame(ProgressionGame game) {
        if (game == null) {
            throw new IllegalArgumentException("ProgressionGame não pode ser nulo.");
        }
    }
}
