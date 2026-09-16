package br.com.laboon.core.progression;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.database.DatabaseManager;
import br.com.laboon.core.progression.prestige.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class PrestigeServiceTest {

    @Test
    void shouldRejectWhenLevelIsNotMax() {
        UUID player = UUID.randomUUID();

        Account account = mock(Account.class);
        AccountManager accountManager = mock(AccountManager.class);
        when(accountManager.get(player)).thenReturn(account);

        ProgressionRepository progressionRepository = mock(ProgressionRepository.class);
        when(progressionRepository.getExperience(player, ProgressionGame.GLOBAL)).thenReturn(0L);

        ProgressionService progressionService =
                new ProgressionService(accountManager, progressionRepository);

        PrestigeRepository prestigeRepository = mock(PrestigeRepository.class);
        when(prestigeRepository.getPrestige(player)).thenReturn(0);

        PrestigeTransactionRepository transactionRepository =
                mock(PrestigeTransactionRepository.class);

        PrestigeService service =
                new PrestigeService(
                        mock(DatabaseManager.class),
                        accountManager,
                        prestigeRepository,
                        transactionRepository,
                        progressionService
                );

        PrestigeResult result = service.prestige(player);

        assertEquals(
                PrestigeResult.Status.REQUIREMENT_NOT_MET,
                result.getStatus()
        );

        verify(transactionRepository, never())
                .setPrestige(
                        any(Connection.class),
                        any(UUID.class),
                        anyInt()
                );
    }

    @Test
    void shouldRejectAtMaximumPrestige() {
        UUID player = UUID.randomUUID();

        AccountManager accountManager = mock(AccountManager.class);
        when(accountManager.get(player)).thenReturn(mock(Account.class));

        ProgressionService progressionService =
                new ProgressionService(
                        accountManager,
                        mock(ProgressionRepository.class)
                );

        PrestigeRepository prestigeRepository = mock(PrestigeRepository.class);
        when(prestigeRepository.getPrestige(player)).thenReturn(100);

        PrestigeService service =
                new PrestigeService(
                        mock(DatabaseManager.class),
                        accountManager,
                        prestigeRepository,
                        mock(PrestigeTransactionRepository.class),
                        progressionService,
                        100
                );

        PrestigeResult result = service.prestige(player);

        assertEquals(
                PrestigeResult.Status.MAX_PRESTIGE,
                result.getStatus()
        );
    }

    @Test
    void shouldCreatePrestigeDefinition() {
        AccountManager accountManager = mock(AccountManager.class);

        ProgressionService progressionService =
                new ProgressionService(
                        accountManager,
                        mock(ProgressionRepository.class)
                );

        PrestigeService service =
                new PrestigeService(
                        mock(DatabaseManager.class),
                        accountManager,
                        mock(PrestigeRepository.class),
                        mock(PrestigeTransactionRepository.class),
                        progressionService,
                        10
                );

        PrestigeDefinition definition = service.definition(3);

        assertEquals(3, definition.prestige());
        assertEquals(1000, definition.requiredLevel());
    }

    @Test
    void shouldNotifyAfterCommit() throws Exception {
        UUID player = UUID.randomUUID();

        Account account = mock(Account.class);
        AccountManager accountManager = mock(AccountManager.class);

        when(accountManager.get(player))
                .thenReturn(account);

        when(account.getExperience())
                .thenReturn(
                        LevelCalculator.defaultCalculator()
                                .requiredExperience(1000)
                );

        ProgressionRepository progressionRepository =
                mock(ProgressionRepository.class);

        when(
                progressionRepository.getExperience(
                        player,
                        ProgressionGame.GLOBAL
                )
        ).thenReturn(
                LevelCalculator.defaultCalculator()
                        .requiredExperience(1000)
        );

        ProgressionService progressionService =
                new ProgressionService(
                        accountManager,
                        progressionRepository
                );

        PrestigeRepository prestigeRepository =
                mock(PrestigeRepository.class);

        when(
                prestigeRepository.getPrestige(player)
        ).thenReturn(0);

        PrestigeTransactionRepository transactionRepository =
                mock(PrestigeTransactionRepository.class);

        when(
                transactionRepository.getPrestige(
                        any(Connection.class),
                        eq(player)
                )
        ).thenReturn(0);

        DatabaseManager database =
                mock(DatabaseManager.class);

        Connection connection =
                mock(Connection.class);

        PreparedStatement statement =
                mock(PreparedStatement.class);

        when(
                database.getConnection()
        ).thenReturn(connection);

        when(
                connection.prepareStatement(anyString())
        ).thenReturn(statement);

        when(
                statement.executeUpdate()
        ).thenReturn(1);

        PrestigeService service =
                new PrestigeService(
                        database,
                        accountManager,
                        prestigeRepository,
                        transactionRepository,
                        progressionService
                );

        final PrestigeLevelUpEvent[] captured =
                new PrestigeLevelUpEvent[1];

        service.addListener(
                event -> captured[0] = event
        );

        PrestigeResult result =
                service.prestige(player);

        assertEquals(
                PrestigeResult.Status.SUCCESS,
                result.getStatus()
        );

        assertNotNull(captured[0]);

        assertEquals(
                0,
                captured[0].getPreviousPrestige()
        );

        assertEquals(
                1,
                captured[0].getNewPrestige()
        );

        verify(
                transactionRepository
        ).getPrestige(
                connection,
                player
        );

        verify(
                transactionRepository
        ).setPrestige(
                connection,
                player,
                1
        );

        verify(connection).commit();

        verify(account).setExperience(0L);
    }
}
