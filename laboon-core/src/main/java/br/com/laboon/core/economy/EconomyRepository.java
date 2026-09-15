package br.com.laboon.core.economy;

import java.util.List;
import java.util.UUID;

public interface EconomyRepository {

    EconomyAccount find(UUID playerUuid);

    EconomyAccount getOrCreate(UUID playerUuid);

    void save(EconomyAccount account);

    boolean exists(UUID playerUuid);

    void delete(UUID playerUuid);

    void saveTransaction(
            EconomyTransaction transaction
    );

    void saveWithTransaction(
            EconomyAccount account,
            EconomyTransaction transaction
    );

    List<EconomyTransaction> getTransactions(
            UUID playerUuid,
            int limit
    );
}