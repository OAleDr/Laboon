package br.com.laboon.core.progression;

import java.time.Instant;
import java.util.UUID;

public record ProgressionXpEntry(
        UUID id,
        UUID playerUuid,
        ProgressionGame game,
        long amount,
        ExperienceSource source,
        String metadata,
        Instant createdAt
) {
}
