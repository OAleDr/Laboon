package br.com.laboon.velocity.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public final class MojangProfileService {

    private static final String API_URL =
            "https://api.mojang.com/users/profiles/minecraft/";

    private final HttpClient httpClient;

    public MojangProfileService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Retorna o UUID oficial da conta Mojang/Minecraft
     * quando o nickname existe.
     *
     * Retorna null somente quando o perfil não existe (HTTP 404).
     *
     * Outros erros são propagados porque uma falha na API
     * não significa que o jogador seja cracked.
     */
    public UUID findUuid(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username não pode ser nulo ou vazio."
            );
        }

        String url = API_URL + username;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("User-Agent", "LaboonNetwork/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            /*
             * O jogador não possui perfil nesse nome.
             */
            if (response.statusCode() == 404) {
                return null;
            }

            /*
             * Não vamos considerar indisponibilidade da Mojang
             * como conta cracked.
             */
            if (response.statusCode() != 200) {
                throw new IOException(
                        "Mojang respondeu HTTP "
                                + response.statusCode()
                );
            }

            JsonObject json = JsonParser
                    .parseString(response.body())
                    .getAsJsonObject();

            if (!json.has("id")) {
                return null;
            }

            return parseUuid(
                    json.get("id").getAsString()
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "A consulta à Mojang foi interrompida.",
                    exception
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Não foi possível consultar a Mojang.",
                    exception
            );

        } catch (RuntimeException exception) {

            throw new IllegalStateException(
                    "Resposta inválida da Mojang.",
                    exception
            );
        }
    }

    private UUID parseUuid(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.replace("-", "");

        if (normalized.length() != 32) {
            return null;
        }

        String formatted =
                normalized.substring(0, 8)
                        + "-"
                        + normalized.substring(8, 12)
                        + "-"
                        + normalized.substring(12, 16)
                        + "-"
                        + normalized.substring(16, 20)
                        + "-"
                        + normalized.substring(20, 32);

        return UUID.fromString(formatted);
    }
}