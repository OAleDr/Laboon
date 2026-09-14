package br.com.laboon.core.account.repository;

import br.com.laboon.core.account.Account;

import java.util.UUID;

public interface AccountRepository {

    Account findById(UUID uniqueId);

    Account findByName(String name);

    boolean exists(UUID uniqueId);

    void insert(Account account);

    void update(Account account);

    void delete(UUID uniqueId);
}