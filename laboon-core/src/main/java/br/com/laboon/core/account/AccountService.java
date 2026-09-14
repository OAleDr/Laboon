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

        /*
         * 1. Cache
         */
        Account cached =
                cache.get(uniqueId);

        if (cached != null) {
            return cached;
        }

        /*
         * 2. PostgreSQL
         */
        Account account =
                repository.findById(uniqueId);

        if (account == null) {
            return null;
        }

        /*
         * 3. Cache
         */
        cache.put(account);

        return account;
    }

    public Account findByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        /*
         * Primeiro tenta o índice Redis.
         */
        UUID uuid =
                cache.findUuidByName(name);

        if (uuid != null) {

            Account cached =
                    cache.get(uuid);

            if (cached != null) {
                return cached;
            }
        }

        /*
         * Redis não encontrou.
         * PostgreSQL é a fonte oficial.
         */
        Account account =
                repository.findByName(name);

        if (account != null) {
            cache.put(account);
        }

        return account;
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
                    "Nome da conta não pode ser vazio."
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Tipo da conta não pode ser nulo."
            );
        }

        Account account =
                find(uniqueId);

        if (account != null) {

            boolean changed = false;

            if (!name.equals(account.getName())) {

                account.setName(name);
                changed = true;
            }

            if (account.getType() != type) {

                account.setType(type);
                changed = true;
            }

            account.setLastLogin(
                    Instant.now()
            );

            if (changed || account.getLastLogin() != null) {
                repository.update(account);
            }

            cache.put(account);

            return account;
        }

        account =
                new Account(
                        uniqueId,
                        name,
                        type,
                        Instant.now(),
                        Instant.now(),
                        new AccountPreferences()
                );

        repository.insert(account);

        cache.put(account);

        return account;
    }

    public void save(Account account) {

        if (account == null) {
            return;
        }

        repository.update(account);

        cache.put(account);
    }

    /**
     * Persiste e remove do cache.
     *
     * Deve ser utilizado quando o jogador
     * realmente deixar o servidor.
     */
    public void saveAndUnload(Account account) {

        if (account == null) {
            return;
        }

        /*
         * Primeiro salva no PostgreSQL.
         */
        repository.update(account);

        /*
         * Somente depois remove do Redis.
         */
        cache.remove(account);
    }

    public void unload(Account account) {

        if (account == null) {
            return;
        }

        cache.remove(account);
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
                repository.findById(uniqueId);

        repository.delete(uniqueId);

        if (account != null) {
            cache.remove(account);
        }
    }

    public void updateLastLogin(Account account) {

        if (account == null) {
            return;
        }

        account.setLastLogin(
                Instant.now()
        );

        repository.update(account);
        cache.put(account);
    }
}