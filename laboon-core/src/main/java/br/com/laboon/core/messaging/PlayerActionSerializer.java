package br.com.laboon.core.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerActionSerializer {

    private PlayerActionSerializer() {
    }

    public static String serialize(PlayerActionMessage message) {
        if (message == null) {
            return "";
        }

        return "action=" + message.getAction().name()
                + ";player=" + message.getPlayerUuid()
                + ";target=" + uuid(message.getTargetUuid())
                + ";server=" + safe(message.getServer())
                + ";status=" + status(message.getStatus());
    }

    public static PlayerActionMessage deserialize(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return null;
        }

        Map<String, String> fields = new HashMap<>();

        for (String field : serialized.split(";", -1)) {
            String[] pair = field.split("=", 2);

            if (pair.length == 2) {
                fields.put(pair[0], pair[1]);
            }
        }

        try {
            String actionValue = required(fields, "action");
            String playerValue = required(fields, "player");

            PlayerActionMessage.Action action =
                    PlayerActionMessage.Action.valueOf(actionValue);

            UUID playerUuid = UUID.fromString(playerValue);
            UUID targetUuid = nullableUuid(fields.get("target"));
            String server = nullableString(fields.get("server"));
            PlayerActionMessage.Status status = nullableStatus(fields.get("status"));

            return new PlayerActionMessage(
                    action,
                    playerUuid,
                    targetUuid,
                    server,
                    status
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String required(Map<String, String> fields, String key) {
        String value = fields.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Campo obrigatório ausente: " + key);
        }

        return value;
    }

    private static UUID nullableUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return UUID.fromString(value);
    }

    private static String nullableString(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static PlayerActionMessage.Status nullableStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return PlayerActionMessage.Status.valueOf(value);
    }

    private static String uuid(UUID value) {
        return value == null ? "" : value.toString();
    }

    private static String status(PlayerActionMessage.Status value) {
        return value == null ? "" : value.name();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
