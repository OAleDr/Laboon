package br.com.laboon.core.rewards;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.database.DatabaseManager;
import br.com.laboon.core.economy.EconomyCurrency;
import br.com.laboon.core.economy.EconomyTransaction;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RewardServiceTest {

    @Test
    void shouldRejectNullPlayer() {

        RewardService service =
                createService();

        RewardResult result =
                service.give(
                        null,
                        new Reward().addCoins(100),
                        RewardSource.GAME_WIN
                );

        assertEquals(
                RewardResult.Status.INVALID_PLAYER,
                result.getStatus()
        );
    }

    @Test
    void shouldRejectEmptyReward() {

        RewardService service =
                createService();

        RewardResult result =
                service.give(
                        UUID.randomUUID(),
                        new Reward(),
                        RewardSource.GAME_WIN
                );

        assertEquals(
                RewardResult.Status.EMPTY_REWARD,
                result.getStatus()
        );
    }

    @Test
    void shouldRejectInvalidRewardId() {

        RewardService service =
                createService();

        RewardResult result =
                service.give(
                        UUID.randomUUID(),
                        new Reward().addCoins(100),
                        RewardSource.GAME_WIN,
                        ""
                );

        assertEquals(
                RewardResult.Status.INVALID_REWARD_ID,
                result.getStatus()
        );
    }

    @Test
    void shouldReturnAlreadyClaimed() throws Exception {

        UUID playerUuid =
                UUID.randomUUID();

        DatabaseManager database =
                mock(
                        DatabaseManager.class
                );

        Connection connection =
                mock(
                        Connection.class
                );

        AccountManager accountManager =
                mock(
                        AccountManager.class
                );

        RewardTransactionRepository repository =
                mock(
                        RewardTransactionRepository.class
                );

        when(
                database.getConnection()
        ).thenReturn(
                connection
        );

        when(
                repository.tryClaim(
                        eq(connection),
                        eq("reward-123"),
                        eq(playerUuid),
                        eq(RewardSource.GAME_WIN)
                )
        ).thenReturn(false);

        RewardService service =
                createService(
                        database,
                        accountManager,
                        repository
                );

        RewardResult result =
                service.give(
                        playerUuid,
                        new Reward().addCoins(100),
                        RewardSource.GAME_WIN,
                        "reward-123"
                );

        assertEquals(
                RewardResult.Status.ALREADY_CLAIMED,
                result.getStatus()
        );

        verify(
                repository,
                never()
        ).updateEconomy(
                any(),
                any(),
                any(),
                anyLong()
        );

        verify(
                connection
        ).rollback();
    }

    @Test
    void shouldApplyCoins() throws Exception {

        UUID playerUuid =
                UUID.randomUUID();

        DatabaseManager database =
                mock(
                        DatabaseManager.class
                );

        Connection connection =
                mock(
                        Connection.class
                );

        AccountManager accountManager =
                mock(
                        AccountManager.class
                );

        RewardTransactionRepository repository =
                mock(
                        RewardTransactionRepository.class
                );

        Account account =
                new Account(
                        playerUuid,
                        "TestPlayer"
                );

        when(
                database.getConnection()
        ).thenReturn(
                connection
        );

        when(
                repository.tryClaim(
                        eq(connection),
                        eq("reward-coins"),
                        eq(playerUuid),
                        eq(RewardSource.GAME_WIN)
                )
        ).thenReturn(true);

        when(
                repository.findAccount(
                        eq(connection),
                        eq(playerUuid)
                )
        ).thenReturn(account);

        RewardService service =
                createService(
                        database,
                        accountManager,
                        repository
                );

        RewardResult result =
                service.give(
                        playerUuid,
                        new Reward().addCoins(100),
                        RewardSource.GAME_WIN,
                        "reward-coins"
                );

        assertTrue(
                result.isSuccess()
        );

        verify(
                repository
        ).updateEconomy(
                eq(connection),
                eq(playerUuid),
                eq(EconomyCurrency.COINS),
                eq(100L)
        );

        verify(
                repository
        ).saveEconomyTransaction(
                eq(connection),
                any(EconomyTransaction.class)
        );

        verify(
                connection
        ).commit();

        verify(
                connection,
                never()
        ).rollback();
    }

    @Test
    void shouldApplyTokens() throws Exception {

        UUID playerUuid =
                UUID.randomUUID();

        DatabaseManager database =
                mock(
                        DatabaseManager.class
                );

        Connection connection =
                mock(
                        Connection.class
                );

        AccountManager accountManager =
                mock(
                        AccountManager.class
                );

        RewardTransactionRepository repository =
                mock(
                        RewardTransactionRepository.class
                );

        Account account =
                new Account(
                        playerUuid,
                        "TestPlayer"
                );

        when(
                database.getConnection()
        ).thenReturn(
                connection
        );

        when(
                repository.tryClaim(
                        eq(connection),
                        eq("reward-tokens"),
                        eq(playerUuid),
                        eq(RewardSource.EVENT)
                )
        ).thenReturn(true);

        when(
                repository.findAccount(
                        eq(connection),
                        eq(playerUuid)
                )
        ).thenReturn(account);

        RewardService service =
                createService(
                        database,
                        accountManager,
                        repository
                );

        RewardResult result =
                service.give(
                        playerUuid,
                        new Reward().addTokens(50),
                        RewardSource.EVENT,
                        "reward-tokens"
                );

        assertTrue(
                result.isSuccess()
        );

        verify(
                repository
        ).updateEconomy(
                eq(connection),
                eq(playerUuid),
                eq(EconomyCurrency.TOKENS),
                eq(50L)
        );

        verify(
                repository
        ).saveEconomyTransaction(
                eq(connection),
                any(EconomyTransaction.class)
        );

        verify(
                connection
        ).commit();

        verify(
                connection,
                never()
        ).rollback();
    }

    @Test
    void shouldApplyExperience() throws Exception {

        UUID playerUuid =
                UUID.randomUUID();

        DatabaseManager database =
                mock(
                        DatabaseManager.class
                );

        Connection connection =
                mock(
                        Connection.class
                );

        AccountManager accountManager =
                mock(
                        AccountManager.class
                );

        RewardTransactionRepository repository =
                mock(
                        RewardTransactionRepository.class
                );

        Account account =
                new Account(
                        playerUuid,
                        "TestPlayer"
                );

        account.setExperience(
                500
        );

        when(
                database.getConnection()
        ).thenReturn(
                connection
        );

        when(
                repository.tryClaim(
                        eq(connection),
                        eq("reward-xp"),
                        eq(playerUuid),
                        eq(RewardSource.GAME_WIN)
                )
        ).thenReturn(true);

        when(
                repository.findAccount(
                        eq(connection),
                        eq(playerUuid)
                )
        ).thenReturn(account);

        RewardService service =
                createService(
                        database,
                        accountManager,
                        repository
                );

        RewardResult result =
                service.give(
                        playerUuid,
                        new Reward().addExperience(250),
                        RewardSource.GAME_WIN,
                        "reward-xp"
                );

        assertTrue(
                result.isSuccess()
        );

        assertEquals(
                750L,
                account.getExperience()
        );

        verify(
                repository
        ).saveAccount(
                eq(connection),
                eq(account)
        );

        verify(
                connection
        ).commit();

        verify(
                connection,
                never()
        ).rollback();
    }

    @Test
    void shouldApplyMultipleRewards() throws Exception {

        UUID playerUuid =
                UUID.randomUUID();

        DatabaseManager database =
                mock(
                        DatabaseManager.class
                );

        Connection connection =
                mock(
                        Connection.class
                );

        AccountManager accountManager =
                mock(
                        AccountManager.class
                );

        RewardTransactionRepository repository =
                mock(
                        RewardTransactionRepository.class
                );

        Account account =
                new Account(
                        playerUuid,
                        "TestPlayer"
                );

        account.setExperience(
                100
        );

        when(
                database.getConnection()
        ).thenReturn(
                connection
        );

        when(
                repository.tryClaim(
                        eq(connection),
                        eq("reward-multi"),
                        eq(playerUuid),
                        eq(RewardSource.GAME_WIN)
                )
        ).thenReturn(true);

        when(
                repository.findAccount(
                        eq(connection),
                        eq(playerUuid)
                )
        ).thenReturn(account);

        RewardService service =
                createService(
                        database,
                        accountManager,
                        repository
                );

        Reward reward =
                new Reward()
                        .addCoins(100)
                        .addTokens(10)
                        .addExperience(250);

        RewardResult result =
                service.give(
                        playerUuid,
                        reward,
                        RewardSource.GAME_WIN,
                        "reward-multi"
                );

        assertTrue(
                result.isSuccess()
        );

        assertEquals(
                350L,
                account.getExperience()
        );

        verify(
                repository
        ).updateEconomy(
                eq(connection),
                eq(playerUuid),
                eq(EconomyCurrency.COINS),
                eq(100L)
        );

        verify(
                repository
        ).updateEconomy(
                eq(connection),
                eq(playerUuid),
                eq(EconomyCurrency.TOKENS),
                eq(10L)
        );

        verify(
                repository,
                times(2)
        ).saveEconomyTransaction(
                eq(connection),
                any(EconomyTransaction.class)
        );

        verify(
                repository
        ).saveAccount(
                eq(connection),
                eq(account)
        );

        verify(
                connection
        ).commit();
    }

    @Test
    void shouldRollbackWhenEconomyFails() throws Exception {

        UUID playerUuid =
                UUID.randomUUID();

        DatabaseManager database =
                mock(
                        DatabaseManager.class
                );

        Connection connection =
                mock(
                        Connection.class
                );

        AccountManager accountManager =
                mock(
                        AccountManager.class
                );

        RewardTransactionRepository repository =
                mock(
                        RewardTransactionRepository.class
                );

        Account account =
                new Account(
                        playerUuid,
                        "TestPlayer"
                );

        when(
                database.getConnection()
        ).thenReturn(
                connection
        );

        when(
                repository.tryClaim(
                        eq(connection),
                        eq("reward-failure"),
                        eq(playerUuid),
                        eq(RewardSource.GAME_WIN)
                )
        ).thenReturn(true);

        when(
                repository.findAccount(
                        eq(connection),
                        eq(playerUuid)
                )
        ).thenReturn(account);

        doThrow(
                new IllegalStateException(
                        "Economy failure"
                )
        ).when(repository)
                .updateEconomy(
                        eq(connection),
                        eq(playerUuid),
                        eq(EconomyCurrency.COINS),
                        eq(100L)
                );

        RewardService service =
                createService(
                        database,
                        accountManager,
                        repository
                );

        RewardResult result =
                service.give(
                        playerUuid,
                        new Reward().addCoins(100),
                        RewardSource.GAME_WIN,
                        "reward-failure"
                );

        assertEquals(
                RewardResult.Status.FAILED,
                result.getStatus()
        );

        verify(
                connection
        ).rollback();

        verify(
                connection,
                never()
        ).commit();
    }

    @Test
    void shouldRollbackWhenAccountSaveFails() throws Exception {

        UUID playerUuid =
                UUID.randomUUID();

        DatabaseManager database =
                mock(
                        DatabaseManager.class
                );

        Connection connection =
                mock(
                        Connection.class
                );

        AccountManager accountManager =
                mock(
                        AccountManager.class
                );

        RewardTransactionRepository repository =
                mock(
                        RewardTransactionRepository.class
                );

        Account account =
                new Account(
                        playerUuid,
                        "TestPlayer"
                );

        when(
                database.getConnection()
        ).thenReturn(
                connection
        );

        when(
                repository.tryClaim(
                        eq(connection),
                        eq("reward-xp-failure"),
                        eq(playerUuid),
                        eq(RewardSource.GAME_WIN)
                )
        ).thenReturn(true);

        when(
                repository.findAccount(
                        eq(connection),
                        eq(playerUuid)
                )
        ).thenReturn(account);

        doThrow(
                new IllegalStateException(
                        "Account save failure"
                )
        ).when(repository)
                .saveAccount(
                        eq(connection),
                        eq(account)
                );

        RewardService service =
                createService(
                        database,
                        accountManager,
                        repository
                );

        RewardResult result =
                service.give(
                        playerUuid,
                        new Reward().addExperience(100),
                        RewardSource.GAME_WIN,
                        "reward-xp-failure"
                );

        assertEquals(
                RewardResult.Status.FAILED,
                result.getStatus()
        );

        verify(
                connection
        ).rollback();

        verify(
                connection,
                never()
        ).commit();
    }

    private RewardService createService() {

        DatabaseManager database =
                mock(
                        DatabaseManager.class
                );

        AccountManager accountManager =
                mock(
                        AccountManager.class
                );

        RewardTransactionRepository repository =
                mock(
                        RewardTransactionRepository.class
                );

        return createService(
                database,
                accountManager,
                repository
        );
    }

    private RewardService createService(
            DatabaseManager database,
            AccountManager accountManager,
            RewardTransactionRepository repository
    ) {

        return new RewardService(
                database,
                accountManager,
                repository
        );
    }
}