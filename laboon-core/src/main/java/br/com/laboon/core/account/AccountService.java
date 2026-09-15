package br.com.laboon.core.account;

import br.com.laboon.core.account.cache.AccountCache;
import br.com.laboon.core.account.repository.AccountRepository;
import br.com.laboon.core.account.repository.PostgreSqlAccountPreferencesRepository;
import br.com.laboon.core.account.repository.PostgreSqlPunishmentRepository;
import br.com.laboon.core.account.repository.PostgreSqlTemporaryGroupRepository;

import java.time.Instant;
import java.util.UUID;

public final class AccountService {

    private final AccountRepository repository;
    private final AccountCache cache;

    private final PostgreSqlAccountPreferencesRepository preferencesRepository;
    private final PostgreSqlTemporaryGroupRepository temporaryGroupRepository;
    private final PostgreSqlPunishmentRepository punishmentRepository;

    public AccountService(
            AccountRepository repository,
            AccountCache cache,
            PostgreSqlAccountPreferencesRepository preferencesRepository,
            PostgreSqlTemporaryGroupRepository temporaryGroupRepository,
            PostgreSqlPunishmentRepository punishmentRepository
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
        this.preferencesRepository = preferencesRepository;
        this.temporaryGroupRepository = temporaryGroupRepository;
        this.punishmentRepository = punishmentRepository;
    }

    public Account find(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        Account cached =
                cache.get(uniqueId);

        if (cached != null) {
            return cached;
        }

        Account account =
                repository.findById(uniqueId);

        if (account == null) {
            return null;
        }

        loadExtraData(account);

        cache.put(account);

        return account;
    }

    public Account findByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        UUID uuid =
                cache.findUuidByName(name);

        if (uuid != null) {

            Account cached =
                    cache.get(uuid);

            if (cached != null) {
                return cached;
            }
        }

        Account account =
                repository.findByName(name);

        if (account == null) {
            return null;
        }

        loadExtraData(account);

        cache.put(account);

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

        validate(
                uniqueId,
                name,
                type
        );

        Account account =
                find(uniqueId);

        if (account != null) {

            boolean changed = false;

            if (!name.equals(
                    account.getName()
            )) {

                account.setName(name);
                changed = true;
            }

            if (account.getType() != type) {

                account.setType(type);
                changed = true;
            }

            if (changed) {
                save(account);
            }

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

        saveExtraData(account);

        return account;
    }

    public void save(Account account) {

        if (account == null) {
            return;
        }

        repository.save(account);

        saveExtraData(account);

        cache.put(account);
    }

    public void saveAndUnload(Account account) {

        if (account == null) {
            return;
        }

        repository.save(account);

        saveExtraData(account);

        cache.remove(account);
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

        if (cache.contains(uniqueId)) {
            return true;
        }

        return repository.exists(uniqueId);
    }

    public void delete(UUID uniqueId) {

        if (uniqueId == null) {
            return;
        }

        repository.delete(uniqueId);

        if (preferencesRepository != null) {
            preferencesRepository.delete(uniqueId);
        }

        if (temporaryGroupRepository != null) {
            temporaryGroupRepository.deleteAll(uniqueId);
        }

        cache.invalidate(uniqueId);
    }

    private void loadExtraData(Account account) {

        if (preferencesRepository != null) {

            AccountPreferences preferences =
                    preferencesRepository.findOrCreate(
                            account.getUniqueId()
                    );

            account.setPreferences(
                    preferences
            );
        }

        if (temporaryGroupRepository != null) {

            account.getTemporaryGroups().keySet()
                    .forEach(account::removeTemporaryGroup);

            temporaryGroupRepository
                    .findAll(account.getUniqueId())
                    .forEach(
                            account::setTemporaryGroup
                    );
        }

        if (punishmentRepository != null) {

            account.setPunishmentHistory(
                    punishmentRepository.findHistory(
                            account.getUniqueId()
                    )
            );
        }
    }

    private void saveExtraData(Account account) {

        if (preferencesRepository != null) {

            preferencesRepository.save(
                    account.getUniqueId(),
                    account.getPreferences()
            );
        }

        if (temporaryGroupRepository != null) {

            temporaryGroupRepository.deleteAll(
                    account.getUniqueId()
            );

            account.getTemporaryGroups()
                    .forEach(
                            (
                                    group,
                                    expiration
                            ) -> temporaryGroupRepository.save(
                                    account.getUniqueId(),
                                    group,
                                    expiration
                            )
                    );
        }

        /*
         * Punições são persistidas individualmente
         * quando forem aplicadas/removidas.
         *
         * Não fazemos DELETE + INSERT do histórico
         * a cada save da Account.
         */
    }

    private void validate(
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
    }
}