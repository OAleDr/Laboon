package br.com.laboon.core.friend;

import java.time.Instant;
import java.util.UUID;

public final class Friend {

    private final UUID uniqueId;
    private final Instant addedAt;

    public Friend(UUID uniqueId, Instant addedAt) {

        if (uniqueId == null) {
            throw new IllegalArgumentException("UUID do amigo não pode ser nulo.");
        }

        if (addedAt == null) {
            throw new IllegalArgumentException("Data de adição não pode ser nula.");
        }

        this.uniqueId = uniqueId;
        this.addedAt = addedAt;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}