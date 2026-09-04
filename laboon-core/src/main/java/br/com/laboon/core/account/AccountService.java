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

        Account account = repository.findById(uniqueId);

        if (account != null) {

            account.setName(name);

            repository.save(account);

            return account;
        }

        account = new Account(uniqueId, name);

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