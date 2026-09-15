package br.com.laboon.core.messaging;

import java.util.UUID;

public record PlayerVanishMessage(
        UUID playerUuid,
        boolean vanished
) {
}
