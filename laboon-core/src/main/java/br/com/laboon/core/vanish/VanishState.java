package br.com.laboon.core.vanish;

import java.util.UUID;

public record VanishState(
        UUID playerUuid,
        boolean vanished
) {
}
