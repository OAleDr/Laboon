package br.com.laboon.core.account;

import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.account.repository.AccountRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class TemporaryGroupService {

    private final AccountRepository accountRepository;

    public TemporaryGroupService(AccountRepository accountRepository) {
        if (accountRepository == null) {
            throw new IllegalArgumentException("AccountRepository não pode ser nulo.");
        }

        this.accountRepository = accountRepository;
    }

    /*
     * =========================
     * SET TEMPORARY GROUP
     * =========================
     */

    public void setTemporaryGroup(Account account, Group group, Duration duration) {
        if (account == null) {
            throw new IllegalArgumentException("Account não pode ser nulo.");
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

        /*
         * Se ainda estiver ativo, adicionamos
         * o novo tempo à expiração existente.
         *
         * Se já estiver expirado, começamos
         * novamente a partir de agora.
         */
        if (currentExpiration != null && currentExpiration.isAfter(now)) {

            startFrom = currentExpiration;
        }

        Instant expiresAt = startFrom.plus(duration);

        account.setTemporaryGroup(group, expiresAt);

        accountRepository.save(account);
    }

    /*
     * =========================
     * REMOVE
     * =========================
     */

    public boolean removeTemporaryGroup(Account account, Group group) {
        if (account == null || group == null) {
            return false;
        }

        if (!account.getTemporaryGroups().containsKey(group)) {
            return false;
        }

        account.removeTemporaryGroup(group);

        accountRepository.save(account);

        return true;
    }

    /*
     * =========================
     * HAS GROUP
     * =========================
     */

    public boolean hasTemporaryGroup(Account account, Group group) {
        if (account == null || group == null) {
            return false;
        }

        return account.hasTemporaryGroup(group);
    }

    /*
     * =========================
     * EXPIRATION
     * =========================
     */

    public Instant getExpiration(Account account, Group group) {
        if (account == null || group == null) {
            return null;
        }

        Instant expiresAt = account.getTemporaryGroups().get(group);

        if (expiresAt == null) {
            return null;
        }

        if (!expiresAt.isAfter(Instant.now())) {
            return null;
        }

        return expiresAt;
    }

    /*
     * =========================
     * ALL GROUPS
     * =========================
     */

    public Map<Group, Instant> getTemporaryGroups(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account não pode ser nulo.");
        }

        return account.getTemporaryGroups();
    }

    /*
     * =========================
     * CLEAN EXPIRED
     * =========================
     */

    public int removeExpiredGroups(Account account) {
        if (account == null) {
            return 0;
        }

        Instant now = Instant.now();

        int removed = 0;

        for (Map.Entry<Group, Instant> entry : account.getTemporaryGroups().entrySet()) {

            Instant expiresAt = entry.getValue();

            if (expiresAt == null || !expiresAt.isAfter(now)) {

                account.removeTemporaryGroup(entry.getKey());

                removed++;
            }
        }

        if (removed > 0) {
            accountRepository.save(account);
        }

        return removed;
    }

    /*
     * =========================
     * UUID
     * =========================
     *
     * Atalho para trabalhar diretamente
     * com o UUID do jogador.
     */

    public boolean removeTemporaryGroup(UUID uniqueId, Group group) {
        if (uniqueId == null || group == null) {
            return false;
        }

        Account account = accountRepository.findById(uniqueId);

        if (account == null) {
            return false;
        }

        return removeTemporaryGroup(account, group);
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }

}