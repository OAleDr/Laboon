package br.com.laboon.core.account.punishment;

import java.time.Instant;
import java.util.UUID;

public final class Kick {

    private final String kickedBy;
    private final UUID kickedByUniqueId;

    private final String server;
    private final Instant time;
    private final String reason;

    public Kick(String kickedBy, UUID kickedByUniqueId, String server, Instant time, String reason) {

        if (kickedBy == null || kickedBy.isBlank()) {
            throw new IllegalArgumentException("Responsável pelo kick não pode ser nulo.");
        }

        if (kickedByUniqueId == null) {
            throw new IllegalArgumentException("UUID do responsável não pode ser nulo.");
        }

        if (time == null) {
            throw new IllegalArgumentException("Data do kick não pode ser nula.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Motivo do kick não pode ser vazio.");
        }

        this.kickedBy = kickedBy;
        this.kickedByUniqueId = kickedByUniqueId;
        this.server = server;
        this.time = time;
        this.reason = reason;
    }

    public String getKickedBy() {
        return kickedBy;
    }

    public UUID getKickedByUniqueId() {
        return kickedByUniqueId;
    }

    public String getServer() {
        return server;
    }

    public Instant getTime() {
        return time;
    }

    public String getReason() {
        return reason;
    }
}