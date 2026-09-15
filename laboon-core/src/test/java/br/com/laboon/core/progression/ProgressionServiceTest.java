package br.com.laboon.core.progression;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProgressionServiceTest {

    @Test
    void shouldAddGlobalExperience() {

        UUID uuid =
                UUID.randomUUID();

        AccountManager accountManager =
                mock(AccountManager.class);

        Account account =
                new Account(
                        uuid,
                        "TestPlayer"
                );

        when(
                accountManager.get(
                        eq(uuid)
                )
        ).thenReturn(account);

        ProgressionRepository repository =
                new InMemoryProgressionRepository();

        ProgressionService service =
                new ProgressionService(
                        accountManager,
                        repository
                );

        ProgressionResult result =
                service.addExperience(
                        uuid,
                        1300L,
                        ExperienceSource.GAME_WIN
                );

        assertTrue(
                result.isSuccess()
        );

        assertEquals(
                1300L,
                account.getExperience()
        );

        assertEquals(
                2,
                result
                        .getSnapshot()
                        .getLevel()
        );

        assertEquals(
                1,
                result
                        .getLevelsGained()
                        .size()
        );

        assertEquals(
                2,
                result
                        .getLevelsGained()
                        .get(0)
        );

        verify(
                accountManager
        ).save(
                eq(account)
        );
    }

    @Test
    void shouldAddGameExperience() {

        UUID uuid =
                UUID.randomUUID();

        AccountManager accountManager =
                mock(AccountManager.class);

        when(
                accountManager.get(
                        eq(uuid)
                )
        ).thenReturn(
                new Account(
                        uuid,
                        "TestPlayer"
                )
        );

        InMemoryProgressionRepository repository =
                new InMemoryProgressionRepository();

        ProgressionService service =
                new ProgressionService(
                        accountManager,
                        repository
                );

        ProgressionResult result =
                service.addExperience(
                        uuid,
                        500L,
                        ProgressionGame.BEDWARS,
                        ExperienceSource.GAME_WIN
                );

        assertTrue(
                result.isSuccess()
        );

        assertEquals(
                500L,
                repository.getExperience(
                        uuid,
                        ProgressionGame.BEDWARS
                )
        );
    }

    @Test
    void shouldRejectInvalidAmount() {

        UUID uuid =
                UUID.randomUUID();

        AccountManager accountManager =
                mock(AccountManager.class);

        ProgressionRepository repository =
                new InMemoryProgressionRepository();

        ProgressionService service =
                new ProgressionService(
                        accountManager,
                        repository
                );

        ProgressionResult result =
                service.addExperience(
                        uuid,
                        0L,
                        ExperienceSource.GAME_WIN
                );

        assertEquals(
                ProgressionResult.Status.INVALID_AMOUNT,
                result.getStatus()
        );
    }

    @Test
    void shouldNotifyLevelUp() {

        UUID uuid =
                UUID.randomUUID();

        AccountManager accountManager =
                mock(AccountManager.class);

        when(
                accountManager.get(
                        eq(uuid)
                )
        ).thenReturn(
                new Account(
                        uuid,
                        "TestPlayer"
                )
        );

        ProgressionRepository repository =
                new InMemoryProgressionRepository();

        ProgressionService service =
                new ProgressionService(
                        accountManager,
                        repository
                );

        AtomicInteger level =
                new AtomicInteger();

        service.addLevelUpListener(
                (
                        snapshot,
                        newLevel
                ) -> level.set(
                        newLevel
                )
        );

        service.addExperience(
                uuid,
                1300L,
                ExperienceSource.GAME_WIN
        );

        assertEquals(
                2,
                level.get()
        );
    }

    @Test
    void shouldReturnHistory() {

        UUID uuid =
                UUID.randomUUID();

        AccountManager accountManager =
                mock(AccountManager.class);

        when(
                accountManager.get(
                        eq(uuid)
                )
        ).thenReturn(
                new Account(
                        uuid,
                        "TestPlayer"
                )
        );

        InMemoryProgressionRepository repository =
                new InMemoryProgressionRepository();

        ProgressionService service =
                new ProgressionService(
                        accountManager,
                        repository
                );

        service.addExperience(
                uuid,
                100L,
                ExperienceSource.GAME_WIN
        );

        assertEquals(
                1,
                service.getHistory(
                        uuid,
                        ProgressionGame.GLOBAL,
                        10
                ).size()
        );
    }

    private static final class InMemoryProgressionRepository
            implements ProgressionRepository {

        private final Map<String, Long> values =
                new HashMap<>();

        private final Map<UUID, ProgressionXpEntry> history =
                new HashMap<>();

        @Override
        public long getExperience(
                UUID playerUuid,
                ProgressionGame game
        ) {
            return values.getOrDefault(
                    key(
                            playerUuid,
                            game
                    ),
                    0L
            );
        }

        @Override
        public void setExperience(
                UUID playerUuid,
                ProgressionGame game,
                long experience
        ) {
            values.put(
                    key(
                            playerUuid,
                            game
                    ),
                    experience
            );
        }

        @Override
        public void addExperience(
                UUID playerUuid,
                ProgressionGame game,
                long amount
        ) {
            values.merge(
                    key(
                            playerUuid,
                            game
                    ),
                    amount,
                    Long::sum
            );
        }

        @Override
        public void saveHistory(
                ProgressionXpEntry entry
        ) {
            history.put(
                    entry.id(),
                    entry
            );
        }

        @Override
        public java.util.List<ProgressionXpEntry> getHistory(
                UUID playerUuid,
                ProgressionGame game,
                int limit
        ) {
            return history.values()
                    .stream()
                    .filter(
                            entry ->
                                    entry.playerUuid()
                                            .equals(playerUuid)
                                            && entry.game()
                                            == game
                    )
                    .limit(limit)
                    .toList();
        }

        private String key(
                UUID uuid,
                ProgressionGame game
        ) {
            return uuid
                    + ":"
                    + game.name();
        }
    }
}
