package br.com.laboon.core.account.repository;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.group.Group;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class TemporaryGroupService {

    private final PostgreSqlTemporaryGroupRepository repository;

    public TemporaryGroupService(PostgreSqlTemporaryGroupRepository repository) {

        if (repository == null) {
            throw new IllegalArgumentException("Repository não pode ser nulo.");
        }

        this.repository = repository;
    }

    public void load(Account account) {

        if (account == null) {
            return;
        }

        Map<Group, Instant> groups = repository.findAll(account.getUniqueId());

        for (Map.Entry<Group, Instant> entry : groups.entrySet()) {

            account.setTemporaryGroup(entry.getKey(), entry.getValue());
        }
    }

    public void setTemporaryGroup(Account account, Group group, Duration duration) {

        if (account == null) {
            throw new IllegalArgumentException("Account não pode ser nula.");
        }

        if (group == null) {
            throw new IllegalArgumentException("Group não pode ser nulo.");
        }

        if (duration == null || duration.isZero() || duration.isNegative()) {

            throw new IllegalArgumentException("A duração deve ser maior que zero.");
        }

        Instant now = Instant.now();

        Instant currentExpiration = account.getTemporaryGroups().get(group);

        Instant startFrom = now;

        if (currentExpiration != null && currentExpiration.isAfter(now)) {

            startFrom = currentExpiration;
        }

        Instant expiresAt = startFrom.plus(duration);

        account.setTemporaryGroup(group, expiresAt);

        repository.save(account.getUniqueId(), group, expiresAt);
    }

    public boolean removeTemporaryGroup(Account account, Group group) {

        if (account == null || group == null) {
            return false;
        }

        if (!account.getTemporaryGroups().containsKey(group)) {

            return false;
        }

        account.removeTemporaryGroup(group);

        repository.delete(account.getUniqueId(), group);

        return true;
    }

    public boolean hasTemporaryGroup(Account account, Group group) {

        if (account == null || group == null) {
            return false;
        }

        Instant expiration = account.getTemporaryGroups().get(group);

        return expiration != null && expiration.isAfter(Instant.now());
    }

    public Instant getExpiration(Account account, Group group) {

        if (account == null || group == null) {
            return null;
        }

        Instant expiresAt = account.getTemporaryGroups().get(group);

        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {

            return null;
        }

        return expiresAt;
    }

    public Map<Group, Instant> getTemporaryGroups(Account account) {

        if (account == null) {
            throw new IllegalArgumentException("Account não pode ser nula.");
        }

        return account.getTemporaryGroups();
    }

    public int removeExpiredGroups(Account account) {

        if (account == null) {
            return 0;
        }

        Instant now = Instant.now();

        int removed = 0;

        for (Map.Entry<Group, Instant> entry : account.getTemporaryGroups().entrySet()) {

            Instant expiresAt = entry.getValue();

            if (expiresAt == null || !expiresAt.isAfter(now)) {

                Group group = entry.getKey();

                account.removeTemporaryGroup(group);

                repository.delete(account.getUniqueId(), group);

                removed++;
            }
        }

        return removed;
    }

    public void deleteAll(Account account) {

        if (account == null) {
            return;
        }

        repository.deleteAll(account.getUniqueId());

        for (Group group : account.getTemporaryGroups().keySet()) {

            account.removeTemporaryGroup(group);
        }
    }

    public boolean removeTemporaryGroup(UUID uniqueId, Group group) {

        if (uniqueId == null || group == null) {
            return false;
        }

        return repositoryFindAndRemove(uniqueId, group);
    }

    private boolean repositoryFindAndRemove(UUID uniqueId, Group group) {

        Map<Group, Instant> groups = repository.findAll(uniqueId);

        if (!groups.containsKey(group)) {
            return false;
        }

        repository.delete(uniqueId, group);

        return true;
    }
}