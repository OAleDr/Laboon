package br.com.laboon.core.messaging;

import java.util.UUID;

public final class PlayerVanishSerializer {

    private PlayerVanishSerializer() {
    }

    public static String serialize(PlayerVanishMessage message) {
        if (message == null || message.playerUuid() == null) {
            return "";
        }

        return "player=" + message.playerUuid()
                + ";vanished=" + message.vanished();
    }

    public static PlayerVanishMessage deserialize(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        UUID playerUuid = null;
        Boolean vanished = null;

        for (String field : message.split(";", -1)) {
            String[] pair = field.split("=", 2);

            if (pair.length != 2) {
                continue;
            }

            try {
                switch (pair[0]) {
                    case "player" -> playerUuid = UUID.fromString(pair[1]);
                    case "vanished" -> vanished = Boolean.parseBoolean(pair[1]);
                    default -> {
                    }
                }
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        if (playerUuid == null || vanished == null) {
            return null;
        }

        return new PlayerVanishMessage(playerUuid, vanished);
    }
}
