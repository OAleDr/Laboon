package br.com.laboon.core.economy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class EconomyService {

    private final EconomyRepository repository;

    public EconomyService(
            EconomyRepository repository
    ) {

        if (repository == null) {
            throw new IllegalArgumentException(
                    "EconomyRepository não pode ser nulo."
            );
        }

        this.repository = repository;
    }

    public EconomyAccount get(
            UUID playerUuid
    ) {

        validateUuid(playerUuid);

        return repository.getOrCreate(
                playerUuid
        );
    }

    public long balance(
            UUID playerUuid,
            EconomyCurrency currency
    ) {

        if (currency == null) {
            return 0L;
        }

        return get(playerUuid)
                .getBalance(currency);
    }

    public EconomyResult deposit(
            UUID playerUuid,
            EconomyCurrency currency,
            long amount,
            String source
    ) {

        if (currency == null) {
            return EconomyResult.failure(
                    EconomyResult.Status.ERROR
            );
        }

        if (amount <= 0L) {
            return EconomyResult.failure(
                    EconomyResult.Status.INVALID_AMOUNT
            );
        }

        EconomyAccount account =
                get(playerUuid);

        account.deposit(
                currency,
                amount
        );

        repository.save(account);

        repository.saveTransaction(
                new EconomyTransaction(
                        UUID.randomUUID(),
                        playerUuid,
                        currency,
                        amount,
                        EconomyTransactionType.DEPOSIT,
                        normalizeSource(source),
                        null,
                        Instant.now()
                )
        );

        return EconomyResult.success(
                account.getBalance(currency)
        );
    }

    public EconomyResult reward(
            UUID playerUuid,
            EconomyCurrency currency,
            long amount,
            String source,
            String metadata
    ) {

        if (currency == null) {
            return EconomyResult.failure(
                    EconomyResult.Status.ERROR
            );
        }

        if (amount <= 0L) {
            return EconomyResult.failure(
                    EconomyResult.Status.INVALID_AMOUNT
            );
        }

        EconomyAccount account =
                get(playerUuid);

        account.deposit(
                currency,
                amount
        );

        repository.save(account);

        repository.saveTransaction(
                new EconomyTransaction(
                        UUID.randomUUID(),
                        playerUuid,
                        currency,
                        amount,
                        EconomyTransactionType.REWARD,
                        normalizeSource(source),
                        metadata,
                        Instant.now()
                )
        );

        return EconomyResult.success(
                account.getBalance(currency)
        );
    }

    public EconomyResult withdraw(
            UUID playerUuid,
            EconomyCurrency currency,
            long amount,
            String source
    ) {

        if (currency == null) {
            return EconomyResult.failure(
                    EconomyResult.Status.ERROR
            );
        }

        if (amount <= 0L) {
            return EconomyResult.failure(
                    EconomyResult.Status.INVALID_AMOUNT
            );
        }

        EconomyAccount account =
                get(playerUuid);

        if (!account.withdraw(
                currency,
                amount
        )) {

            return EconomyResult.failure(
                    EconomyResult.Status.INSUFFICIENT_FUNDS
            );
        }

        repository.save(account);

        repository.saveTransaction(
                new EconomyTransaction(
                        UUID.randomUUID(),
                        playerUuid,
                        currency,
                        amount,
                        EconomyTransactionType.WITHDRAW,
                        normalizeSource(source),
                        null,
                        Instant.now()
                )
        );

        return EconomyResult.success(
                account.getBalance(currency)
        );
    }

    public EconomyResult purchase(
            UUID playerUuid,
            EconomyCurrency currency,
            long amount,
            String source,
            String metadata
    ) {

        if (currency == null) {
            return EconomyResult.failure(
                    EconomyResult.Status.ERROR
            );
        }

        if (amount <= 0L) {
            return EconomyResult.failure(
                    EconomyResult.Status.INVALID_AMOUNT
            );
        }

        EconomyAccount account =
                get(playerUuid);

        if (!account.withdraw(
                currency,
                amount
        )) {

            return EconomyResult.failure(
                    EconomyResult.Status.INSUFFICIENT_FUNDS
            );
        }

        repository.save(account);

        repository.saveTransaction(
                new EconomyTransaction(
                        UUID.randomUUID(),
                        playerUuid,
                        currency,
                        amount,
                        EconomyTransactionType.PURCHASE,
                        normalizeSource(source),
                        metadata,
                        Instant.now()
                )
        );

        return EconomyResult.success(
                account.getBalance(currency)
        );
    }

    public List<EconomyTransaction> history(
            UUID playerUuid,
            int limit
    ) {

        validateUuid(playerUuid);

        int safeLimit =
                Math.max(
                        1,
                        Math.min(limit, 100)
                );

        return repository.getTransactions(
                playerUuid,
                safeLimit
        );
    }

    private void validateUuid(
            UUID playerUuid
    ) {

        if (playerUuid == null) {
            throw new IllegalArgumentException(
                    "UUID não pode ser nulo."
            );
        }
    }

    private String normalizeSource(
            String source
    ) {

        if (source == null || source.isBlank()) {
            return "unknown";
        }

        return source.trim();
    }
}