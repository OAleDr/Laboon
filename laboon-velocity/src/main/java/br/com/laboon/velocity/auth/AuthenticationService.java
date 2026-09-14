package br.com.laboon.velocity.auth;

import br.com.laboon.core.account.AccountType;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class AuthenticationService {

    private final MojangProfileService mojangProfileService;

    public AuthenticationService(
            MojangProfileService mojangProfileService
    ) {
        if (mojangProfileService == null) {
            throw new IllegalArgumentException(
                    "MojangProfileService não pode ser nulo."
            );
        }

        this.mojangProfileService = mojangProfileService;
    }

    /**
     * Verifica o nickname na Mojang.
     *
     * Perfil encontrado:
     *   ORIGINAL + UUID oficial
     *
     * Perfil não encontrado:
     *   LABOON + UUID offline determinístico
     *
     * Erro de comunicação:
     *   lança exceção; não transforma o jogador
     *   automaticamente em cracked.
     */
    public AuthenticationResult authenticate(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username inválido."
            );
        }

        UUID premiumUuid =
                mojangProfileService.findUuid(username);

        /*
         * =========================
         * ORIGINAL
         * =========================
         */
        if (premiumUuid != null) {

            return new AuthenticationResult(
                    username,
                    premiumUuid,
                    AccountType.ORIGINAL
            );
        }

        /*
         * =========================
         * LABOON / OFFLINE
         * =========================
         */
        UUID offlineUuid =
                UUID.nameUUIDFromBytes(
                        (
                                "OfflinePlayer:"
                                        + username
                        ).getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return new AuthenticationResult(
                username,
                offlineUuid,
                AccountType.LABOON
        );
    }
}