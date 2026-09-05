package br.com.laboon.core.friend;

import java.time.Instant;
import java.util.UUID;

public final class FriendRequest {

    private final UUID sender;
    private final UUID receiver;
    private final Instant createdAt;

    public FriendRequest(UUID sender, UUID receiver, Instant createdAt) {

        if (sender == null) {
            throw new IllegalArgumentException("Remetente não pode ser nulo.");
        }

        if (receiver == null) {
            throw new IllegalArgumentException("Destinatário não pode ser nulo.");
        }

        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Jogador não pode enviar solicitação para si mesmo.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Data da solicitação não pode ser nula.");
        }

        this.sender = sender;
        this.receiver = receiver;
        this.createdAt = createdAt;
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}