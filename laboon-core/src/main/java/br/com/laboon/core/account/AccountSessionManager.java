package br.com.laboon.core.account;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AccountSessionManager {

    private final ConcurrentMap<
            UUID,
            AccountSession
            > sessions =
            new ConcurrentHashMap<>();

    public void create(
            UUID playerUuid,
            UUID accountUuid
    ) {

        sessions.put(
                playerUuid,
                new AccountSession(
                        playerUuid,
                        accountUuid
                )
        );
    }

    public AccountSession get(
            UUID playerUuid
    ) {

        return sessions.get(playerUuid);
    }

    public void markDirty(
            UUID playerUuid
    ) {

        AccountSession session =
                sessions.get(playerUuid);

        if (session != null) {
            session.markDirty();
        }
    }

    public void markClean(
            UUID playerUuid
    ) {

        AccountSession session =
                sessions.get(playerUuid);

        if (session != null) {
            session.markClean();
        }
    }

    public void remove(
            UUID playerUuid
    ) {

        sessions.remove(playerUuid);
    }

    public boolean isLogged(
            UUID playerUuid
    ) {

        return sessions.containsKey(playerUuid);
    }

    public void clear() {
        sessions.clear();
    }
}