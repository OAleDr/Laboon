package br.com.laboon.bukkit.api.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinService {

    private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";

    private static final String SESSION_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private static final long CACHE_TIME_MILLIS = Duration.ofHours(1).toMillis();

    private final JavaPlugin plugin;

    private final HttpClient httpClient;

    private final Map<String, CachedSkin> cache = new ConcurrentHashMap<>();

    public SkinService(JavaPlugin plugin) {

        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }

        this.plugin = plugin;

        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    /**
     * Busca uma skin pelo nome do jogador.
     * <p>
     * A busca é realizada de forma assíncrona.
     *
     * @param name nome do jogador
     * @return Future contendo a skin ou null caso não encontrada
     */
    public CompletableFuture<Skin> fetch(String name) {

        if (name == null || name.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        String normalizedName = name.trim().toLowerCase();

        CachedSkin cached = cache.get(normalizedName);

        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture(cached.skin);
        }

        return CompletableFuture.supplyAsync(() -> fetchSkin(normalizedName)).thenApply(skin -> {

            if (skin != null) {
                cache.put(normalizedName, new CachedSkin(skin, System.currentTimeMillis()));
            }

            return skin;
        });
    }

    /**
     * Busca uma skin pelo nome.
     * <p>
     * Este método é síncrono.
     * <p>
     * Evite utilizá-lo na thread principal do Bukkit.
     *
     * @param name nome do jogador
     * @return skin encontrada ou null
     */
    public Skin get(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        String normalizedName = name.trim().toLowerCase();

        CachedSkin cached = cache.get(normalizedName);

        if (cached != null && !cached.isExpired()) {
            return cached.skin;
        }

        Skin skin = fetchSkin(normalizedName);

        if (skin != null) {
            cache.put(normalizedName, new CachedSkin(skin, System.currentTimeMillis()));
        }

        return skin;
    }

    /**
     * Remove uma skin específica do cache.
     */
    public void invalidate(String name) {

        if (name == null || name.isBlank()) {
            return;
        }

        cache.remove(name.trim().toLowerCase());
    }

    /**
     * Limpa todo o cache.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Verifica se uma skin está armazenada no cache.
     */
    public boolean isCached(String name) {

        if (name == null || name.isBlank()) {
            return false;
        }

        CachedSkin cached = cache.get(name.trim().toLowerCase());

        return cached != null && !cached.isExpired();
    }

    /**
     * Quantidade de skins atualmente armazenadas.
     */
    public int getCacheSize() {
        return cache.size();
    }

    private Skin fetchSkin(String name) {

        try {

            UUID uniqueId = fetchUniqueId(name);

            if (uniqueId == null) {
                return null;
            }

            return fetchSkin(uniqueId, name);

        } catch (Exception exception) {

            plugin.getLogger().warning("Não foi possível buscar a skin de '" + name + "': " + exception.getMessage());

            return null;
        }
    }

    private UUID fetchUniqueId(String name) throws IOException, InterruptedException {

        URI uri = URI.create(PROFILE_URL + name);

        HttpRequest request = HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(5)).header("Accept", "application/json").GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            return null;
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        if (!json.has("id")) {
            return null;
        }

        String id = json.get("id").getAsString();

        return parseUuid(id);
    }

    private Skin fetchSkin(UUID uniqueId, String name) throws IOException, InterruptedException {

        URI uri = URI.create(SESSION_URL + uniqueId);

        HttpRequest request = HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(5)).header("Accept", "application/json").GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            return null;
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        if (!json.has("properties")) {
            return null;
        }

        var properties = json.getAsJsonArray("properties");

        for (var element : properties) {

            JsonObject property = element.getAsJsonObject();

            if (!property.has("name")) {
                continue;
            }

            if (!"textures".equals(property.get("name").getAsString())) {
                continue;
            }

            if (!property.has("value")) {
                continue;
            }

            String value = property.get("value").getAsString();

            String signature = property.has("signature") ? property.get("signature").getAsString() : null;

            return new Skin(uniqueId, name, value, signature);
        }

        return null;
    }

    private UUID parseUuid(String value) {

        if (value == null || value.length() != 32) {

            return null;
        }

        String formatted = value.substring(0, 8) + "-" + value.substring(8, 12) + "-" + value.substring(12, 16) + "-" + value.substring(16, 20) + "-" + value.substring(20);

        try {
            return UUID.fromString(formatted);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static final class CachedSkin {

        private final Skin skin;
        private final long cachedAt;

        private CachedSkin(Skin skin, long cachedAt) {
            this.skin = skin;
            this.cachedAt = cachedAt;
        }

        private boolean isExpired() {

            return System.currentTimeMillis() - cachedAt >= CACHE_TIME_MILLIS;
        }
    }
}