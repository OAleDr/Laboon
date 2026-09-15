package br.com.laboon.core.progression;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
        ).thenReturn(
                account
        );

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

    private static final class InMemoryProgressionRepository
            implements ProgressionRepository {

        private final Map<String, Long> values =
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