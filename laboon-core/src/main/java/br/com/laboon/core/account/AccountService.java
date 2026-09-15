package br.com.laboon.core.account;

import br.com.laboon.core.account.cache.AccountCache;
import br.com.laboon.core.account.repository.AccountRepository;

import java.time.Instant;
import java.util.UUID;

public final class AccountService {

    private final AccountRepository repository;
    private final AccountCache cache;

    public AccountService(
            AccountRepository repository,
            AccountCache cache
    ) {
        if (repository == null) {
            throw new IllegalArgumentException(
                    "AccountRepository não pode ser nulo."
            );
        }

        if (cache == null) {
            throw new IllegalArgumentException(
                    "AccountCache não pode ser nulo."
            );
        }

        this.repository = repository;
        this.cache = cache;
    }

    public Account find(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        Account cached = cache.get(uniqueId);

        if (cached != null) {
            return cached;
        }

        Account account =
                repository.findById(uniqueId);

        if (account != null) {
            cache.put(account);
        }

        return account;
    }

    public Account findByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        UUID uuid =
                cache.findUuidByName(name);

        if (uuid != null) {

            Account cached = cache.get(uuid);

            if (cached != null) {
                return cached;
            }
        }

        Account account =
                repository.findByName(name);

        if (account != null) {
            cache.put(account);
        }

        return account;
    }

    public Account getOrCreateOriginal(
            UUID uniqueId,
            String name
    ) {
        return getOrCreate(
                uniqueId,
                name,
                AccountType.ORIGINAL
        );
    }

    public Account getOrCreateLaboon(
            UUID uniqueId,
            String name
    ) {
        return getOrCreate(
                uniqueId,
                name,
                AccountType.LABOON
        );
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

        Account account = find(uniqueId);

        if (account != null) {

            account.setName(name);
            account.setType(type);

            save(account);

            return account;
        }

        account =
                new Account(
                        uniqueId,
                        name,
                        type,
                        Instant.now(),
                        null,
                        new AccountPreferences()
                );

        save(account);

        return account;
    }

    public void saveAndUnload(Account account) {

        if (account == null) {
            return;
        }

        repository.save(account);
        cache.remove(account);
    }

    public void save(Account account) {

        if (account == null) {
            return;
        }

        repository.save(account);
        cache.put(account);
    }

    public void updateLastLogin(Account account) {

        if (account == null) {
            return;
        }

        account.setLastLogin(
                Instant.now()
        );

        save(account);
    }

    public boolean exists(UUID uniqueId) {

        if (uniqueId == null) {
            return false;
        }

        if (cache.get(uniqueId) != null) {
            return true;
        }

        return repository.exists(uniqueId);
    }

    public void delete(UUID uniqueId) {

        if (uniqueId == null) {
            return;
        }

        Account account =
                cache.get(uniqueId);

        repository.delete(uniqueId);

        if (account != null) {
            cache.remove(account);
        } else {
            cache.remove(uniqueId, null);
        }
    }
}