package br.com.laboon.core.account;

import java.util.UUID;

/**
 * API pública de gerenciamento de Accounts.
 */
public final class AccountManager {

    private final AccountService service;

    public AccountManager(AccountService service) {

        if (service == null) {
            throw new IllegalArgumentException(
                    "AccountService não pode ser nulo."
            );
        }

        this.service = service;
    }

    public Account get(UUID uniqueId) {

        return service.find(uniqueId);
    }

    public Account getByName(String name) {

        return service.findByName(name);
    }

    public Account getOrCreateOriginal(
            UUID uniqueId,
            String name
    ) {

        return service.getOrCreateOriginal(
                uniqueId,
                name
        );
    }

    public Account getOrCreateLaboon(
            UUID uniqueId,
            String name
    ) {

        return service.getOrCreateLaboon(
                uniqueId,
                name
        );
    }

    public Account getOrCreate(
            UUID uniqueId,
            String name,
            AccountType type
    ) {

        return service.getOrCreate(
                uniqueId,
                name,
                type
        );
    }

    /**
     * Salva mantendo no Redis.
     */
    public void save(Account account) {

        service.save(account);
    }

    /**
     * Salva no PostgreSQL e descarrega do Redis.
     */
    public void saveAndUnload(Account account) {

        service.saveAndUnload(account);
    }

    /**
     * Descarrega a Account:
     *
     * PostgreSQL
     *     ↓
     * Redis remove
     */
    public void unload(Account account) {

        service.saveAndUnload(account);
    }

    public void updateLastLogin(Account account) {

        service.updateLastLogin(account);
    }

    public boolean exists(UUID uniqueId) {

        return service.exists(uniqueId);
    }

    public void delete(UUID uniqueId) {

        service.delete(uniqueId);
    }
}