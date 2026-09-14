package br.com.laboon.core.account;

import br.com.laboon.core.redis.RedisManager;

import java.time.Instant;
import java.util.UUID;

public final class AccountService {

    private final AccountRepository repository;

    public AccountService(RedisManager redisManager) {
        this.repository = new AccountRepository(redisManager);
    }

    public Account find(UUID uniqueId) {
        return repository.findById(uniqueId);
    }

    public Account findByName(String name) {
        return repository.findByName(name);
    }

    public Account getOrCreateOriginal(UUID uniqueId, String name) {
        return getOrCreate(uniqueId, name, AccountType.ORIGINAL);
    }

    public Account getOrCreateLaboon(UUID uniqueId, String name) {
        return getOrCreate(uniqueId, name, AccountType.LABOON);
    }

    public Account getOrCreate(
            UUID uniqueId,
            String name,
            AccountType type
    ) {

        if (uniqueId == null) {
            throw new IllegalArgumentException(
                    "UUID da conta não pode ser nulo."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome da conta não pode ser nulo ou vazio."
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Tipo da conta não pode ser nulo."
            );
        }

        Account account = repository.findById(uniqueId);

        if (account != null) {

            account.setName(name);
            account.setType(type);

            repository.save(account);

            return account;
        }

        account = new Account(
                uniqueId,
                name,
                type,
                Instant.now(),
                null,
                new AccountPreferences()
        );

        repository.save(account);

        return account;
    }

    public void save(Account account) {
        repository.save(account);
    }

    public void updateLastLogin(Account account) {

        account.setLastLogin(Instant.now());

        repository.save(account);
    }

    public boolean exists(UUID uniqueId) {
        return repository.exists(uniqueId);
    }

    public void delete(UUID uniqueId) {
        repository.delete(uniqueId);
    }
}