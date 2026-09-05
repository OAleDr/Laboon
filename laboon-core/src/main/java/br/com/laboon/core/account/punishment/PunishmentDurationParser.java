package br.com.laboon.core.account.punishment;

import java.time.Duration;
import java.util.Locale;

public final class PunishmentDurationParser {

    private PunishmentDurationParser() {
    }

    public static Duration parse(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Duração não informada.");
        }

        String input = value.trim().toLowerCase(Locale.ROOT);

        if (input.length() < 2) {
            throw new IllegalArgumentException("Duração inválida.");
        }

        String numberPart = input.substring(0, input.length() - 1);

        char unit = input.charAt(input.length() - 1);

        long amount;

        try {
            amount = Long.parseLong(numberPart);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Duração inválida.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Duração deve ser maior que zero.");
        }

        return switch (unit) {

            case 's' -> Duration.ofSeconds(amount);

            case 'm' -> Duration.ofMinutes(amount);

            case 'h' -> Duration.ofHours(amount);

            case 'd' -> Duration.ofDays(amount);

            default -> throw new IllegalArgumentException("Unidade inválida. Use s, m, h ou d.");
        };
    }
}