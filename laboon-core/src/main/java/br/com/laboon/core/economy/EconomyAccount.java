package br.com.laboon.core.economy;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class EconomyAccount {

    private final UUID playerUuid;

    private final Map<EconomyCurrency, Long> balances =
            new EnumMap<>(EconomyCurrency.class);

    public EconomyAccount(UUID playerUuid) {

        if (playerUuid == null) {
            throw new IllegalArgumentException(
                    "UUID não pode ser nulo."
            );
        }

        this.playerUuid = playerUuid;

        for (EconomyCurrency currency : EconomyCurrency.values()) {
            balances.put(currency, 0L);
        }
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public long getBalance(
            EconomyCurrency currency
    ) {

        if (currency == null) {
            return 0L;
        }

        return balances.getOrDefault(
                currency,
                0L
        );
    }

    public void setBalance(
            EconomyCurrency currency,
            long balance
    ) {

        if (currency == null) {
            return;
        }

        balances.put(
                currency,
                Math.max(0L, balance)
        );
    }

    public void deposit(
            EconomyCurrency currency,
            long amount
    ) {

        if (currency == null || amount <= 0L) {
            return;
        }

        long current =
                getBalance(currency);

        long next;

        try {
            next = Math.addExact(
                    current,
                    amount
            );
        } catch (ArithmeticException exception) {
            next = Long.MAX_VALUE;
        }

        setBalance(
                currency,
                next
        );
    }

    public boolean withdraw(
            EconomyCurrency currency,
            long amount
    ) {

        if (currency == null || amount <= 0L) {
            return false;
        }

        long current =
                getBalance(currency);

        if (current < amount) {
            return false;
        }

        setBalance(
                currency,
                current - amount
        );

        return true;
    }
}