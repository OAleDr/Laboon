package br.com.laboon.core.game;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class GameRegistry {

    private final ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();

    /*
     * =========================
     * REGISTRAR
     * =========================
     */

    public void register(Game game) {

        if (game == null) {

            throw new IllegalArgumentException("O jogo não pode ser nulo.");
        }

        Game previous = games.putIfAbsent(game.getId(), game);

        if (previous != null) {

            throw new IllegalStateException("O jogo já está registrado: " + game.getId());
        }
    }

    /*
     * =========================
     * BUSCAR
     * =========================
     */

    public Game get(String gameId) {

        if (gameId == null || gameId.isBlank()) {
            return null;
        }

        return games.get(normalize(gameId));
    }

    /*
     * =========================
     * VERIFICAR
     * =========================
     */

    public boolean contains(String gameId) {

        if (gameId == null || gameId.isBlank()) {
            return false;
        }

        return games.containsKey(normalize(gameId));
    }

    /*
     * =========================
     * REMOVER
     * =========================
     */

    public Game unregister(String gameId) {

        if (gameId == null || gameId.isBlank()) {
            return null;
        }

        return games.remove(normalize(gameId));
    }

    /*
     * =========================
     * TODOS
     * =========================
     */

    public Collection<Game> getAll() {

        return Collections.unmodifiableCollection(games.values());
    }

    /*
     * =========================
     * TAMANHO
     * =========================
     */

    public int size() {

        return games.size();
    }

    /*
     * =========================
     * LIMPAR
     * =========================
     */

    public void clear() {

        games.clear();
    }

    /*
     * =========================
     * NORMALIZAÇÃO
     * =========================
     */

    private String normalize(String value) {

        return value.trim().toLowerCase();
    }
}