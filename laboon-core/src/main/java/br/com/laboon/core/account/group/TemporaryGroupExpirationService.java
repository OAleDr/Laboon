package br.com.laboon.core.account.group;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TemporaryGroupExpirationService {

    private static final Duration WARNING_TIME =
            Duration.ofDays(5);

    private final AccountManager accountManager;

    public TemporaryGroupExpirationService(
            AccountManager accountManager
    ) {

        if (accountManager == null) {
            throw new IllegalArgumentException(
                    "AccountManager não pode ser nulo."
            );
        }

        this.accountManager = accountManager;
    }

    public List<Group> removeExpiredGroups(
            Account account
    ) {

        if (account == null) {
            return List.of();
        }

        Instant now = Instant.now();

        List<Group> expired =
                new ArrayList<>();

        for (Map.Entry<Group, Instant> entry :
                account.getTemporaryGroups().entrySet()) {

            Group group = entry.getKey();
            Instant expiresAt = entry.getValue();

            if (group == null || expiresAt == null) {
                continue;
            }

            if (!expiresAt.isAfter(now)) {
                expired.add(group);
            }
        }

        if (expired.isEmpty()) {
            return List.of();
        }

        for (Group group : expired) {
            account.removeTemporaryGroup(group);
        }

        accountManager.save(account);

        return List.copyOf(expired);
    }

    public List<TemporaryGroupExpiration> getExpiringGroups(
            Account account
    ) {

        if (account == null) {
            return List.of();
        }

        Instant now = Instant.now();

        List<TemporaryGroupExpiration> result =
                new ArrayList<>();

        for (Map.Entry<Group, Instant> entry :
                account.getTemporaryGroups().entrySet()) {

            Group group = entry.getKey();
            Instant expiresAt = entry.getValue();

            if (group == null || expiresAt == null) {
                continue;
            }

            Duration remaining =
                    Duration.between(
                            now,
                            expiresAt
                    );

            if (remaining.isZero()
                    || remaining.isNegative()) {
                continue;
            }

            if (remaining.compareTo(WARNING_TIME) <= 0) {

                result.add(
                        new TemporaryGroupExpiration(
                                group,
                                expiresAt,
                                remaining
                        )
                );
            }
        }

        return List.copyOf(result);
    }

    public record TemporaryGroupExpiration(
            Group group,
            Instant expiresAt,
            Duration remaining
    ) {
    }
}