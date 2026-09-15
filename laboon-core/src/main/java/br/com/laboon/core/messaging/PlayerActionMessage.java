package br.com.laboon.core.messaging;

import java.util.UUID;

public final class PlayerActionMessage {

    public enum Action {
        CONNECT,
        TELEPORT
    }

    public enum Status {
        REQUEST,
        SUCCESS,
        FAILED,
        NOT_FOUND,
        SAME_SERVER
    }

    private final Action action;
    private final UUID playerUuid;
    private final UUID targetUuid;
    private final String server;
    private final Status status;

    public PlayerActionMessage(
            Action action,
            UUID playerUuid,
            UUID targetUuid,
            String server,
            Status status
    ) {
        if (action == null) {
            throw new IllegalArgumentException("Action não pode ser nula.");
        }

        if (playerUuid == null) {
            throw new IllegalArgumentException("playerUuid não pode ser nulo.");
        }

        this.action = action;
        this.playerUuid = playerUuid;
        this.targetUuid = targetUuid;
        this.server = server;
        this.status = status;
    }

    public static PlayerActionMessage connect(UUID playerUuid, String server) {
        return new PlayerActionMessage(
                Action.CONNECT,
                playerUuid,
                null,
                server,
                Status.REQUEST
        );
    }

    public static PlayerActionMessage teleport(UUID playerUuid, UUID targetUuid) {
        return new PlayerActionMessage(
                Action.TELEPORT,
                playerUuid,
                targetUuid,
                null,
                Status.REQUEST
        );
    }

    public static PlayerActionMessage result(
            Action action,
            UUID playerUuid,
            UUID targetUuid,
            String server,
            Status status
    ) {
        return new PlayerActionMessage(
                action,
                playerUuid,
                targetUuid,
                server,
                status
        );
    }

    public Action getAction() {
        return action;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getServer() {
        return server;
    }

    public Status getStatus() {
        return status;
    }
}
