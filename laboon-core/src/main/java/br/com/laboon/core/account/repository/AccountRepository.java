package br.com.laboon.core.account.repository;

import br.com.laboon.core.account.Account;

import java.util.UUID;

public interface AccountRepository {

    Account findById(UUID uniqueId);

    Account findByName(String name);

    void save(Account account);

    boolean exists(UUID uniqueId);

    void delete(UUID uniqueId);
}