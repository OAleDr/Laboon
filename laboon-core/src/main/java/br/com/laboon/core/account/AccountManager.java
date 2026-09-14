package br.com.laboon.core.account;

import java.util.UUID;

public final class AccountManager {

    private final AccountService service;

    public AccountManager(AccountService service) {
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

    public void save(Account account) {
        service.save(account);
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