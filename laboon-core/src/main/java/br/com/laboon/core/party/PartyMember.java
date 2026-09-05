package br.com.laboon.core.party;

import java.time.Instant;
import java.util.UUID;

public final class PartyMember {

    private final UUID uniqueId;
    private final Instant joinedAt;

    public PartyMember(UUID uniqueId, Instant joinedAt) {

        if (uniqueId == null) {
            throw new IllegalArgumentException("UUID do membro não pode ser nulo.");
        }

        if (joinedAt == null) {
            throw new IllegalArgumentException("Data de entrada não pode ser nula.");
        }

        this.uniqueId = uniqueId;
        this.joinedAt = joinedAt;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}