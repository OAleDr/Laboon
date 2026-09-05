package br.com.laboon.core.party;

import java.time.Instant;
import java.util.UUID;

public final class PartyInvite {

    private final UUID partyId;
    private final UUID inviter;
    private final UUID invited;
    private final Instant createdAt;
    private final Instant expiresAt;

    public PartyInvite(UUID partyId, UUID inviter, UUID invited, Instant createdAt, Instant expiresAt) {

        if (partyId == null) {
            throw new IllegalArgumentException("UUID da Party não pode ser nulo.");
        }

        if (inviter == null) {
            throw new IllegalArgumentException("UUID do convidador não pode ser nulo.");
        }

        if (invited == null) {
            throw new IllegalArgumentException("UUID do convidado não pode ser nulo.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Data de criação não pode ser nula.");
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException("Data de expiração não pode ser nula.");
        }

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("A expiração deve ser posterior à criação.");
        }

        this.partyId = partyId;
        this.inviter = inviter;
        this.invited = invited;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getPartyId() {
        return partyId;
    }

    public UUID getInviter() {
        return inviter;
    }

    public UUID getInvited() {
        return invited;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return !expiresAt.isAfter(Instant.now());
    }
}