package br.com.laboon.core.rewards;

import br.com.laboon.core.economy.EconomyCurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Reward {

    private final List<RewardEntry> entries =
            new ArrayList<>();

    public Reward() {
    }

    public Reward addCoins(
            long amount
    ) {

        return add(
                RewardType.COINS,
                amount,
                EconomyCurrency.COINS
        );
    }

    public Reward addTokens(
            long amount
    ) {

        return add(
                RewardType.TOKENS,
                amount,
                EconomyCurrency.TOKENS
        );
    }

    public Reward addExperience(
            long amount
    ) {

        return add(
                RewardType.EXPERIENCE,
                amount,
                null
        );
    }

    public Reward addCosmetic(
            long cosmeticId
    ) {

        return add(
                RewardType.COSMETIC,
                cosmeticId,
                null
        );
    }

    public Reward addTitle(
            long titleId
    ) {

        return add(
                RewardType.TITLE,
                titleId,
                null
        );
    }

    public Reward addItem(
            long itemId
    ) {

        return add(
                RewardType.ITEM,
                itemId,
                null
        );
    }

    public Reward add(
            RewardType type,
            long amount,
            EconomyCurrency currency
    ) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "RewardType não pode ser nulo."
            );
        }

        if (amount <= 0L) {
            throw new IllegalArgumentException(
                    "Quantidade da recompensa deve ser maior que zero."
            );
        }

        entries.add(
                new RewardEntry(
                        type,
                        amount,
                        currency
                )
        );

        return this;
    }

    public List<RewardEntry> getEntries() {

        return Collections.unmodifiableList(
                entries
        );
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public static final class RewardEntry {

        private final RewardType type;
        private final long amount;
        private final EconomyCurrency currency;

        private RewardEntry(
                RewardType type,
                long amount,
                EconomyCurrency currency
        ) {

            this.type = type;
            this.amount = amount;
            this.currency = currency;
        }

        public RewardType getType() {
            return type;
        }

        public long getAmount() {
            return amount;
        }

        public EconomyCurrency getCurrency() {
            return currency;
        }
    }
}