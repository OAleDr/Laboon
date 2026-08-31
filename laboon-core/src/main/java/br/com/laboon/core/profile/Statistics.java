package br.com.laboon.core.profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Statistics {

    private final String game;

    private final String mode;

    private final Map<String, Long> values = new ConcurrentHashMap<>();

    public Statistics(String game) {

        this(game, null);
    }

    public Statistics(String game, String mode) {

        if (game == null || game.isBlank()) {

            throw new IllegalArgumentException("O nome do jogo não pode ser vazio.");
        }

        this.game = game.trim().toLowerCase();

        if (mode == null || mode.isBlank()) {

            this.mode = null;

        } else {

            this.mode = mode.trim().toLowerCase();
        }
    }

    public String getGame() {

        return game;
    }

    public String getMode() {

        return mode;
    }

    public boolean hasMode() {

        return mode != null;
    }

    /*
     * =========================
     * VALORES GENÉRICOS
     * =========================
     */

    public long get(String key) {

        return values.getOrDefault(key, 0L);
    }

    public void set(String key, long value) {

        values.put(key, Math.max(0L, value));
    }

    public void add(String key, long amount) {

        if (amount <= 0) {
            return;
        }

        values.merge(key, amount, Long::sum);
    }

    public void remove(String key, long amount) {

        if (amount <= 0) {
            return;
        }

        values.compute(key, (ignored, current) -> {

            long value = current == null ? 0L : current;

            return Math.max(0L, value - amount);
        });
    }

    /*
     * =========================
     * KILLS
     * =========================
     */

    public long getKills() {

        return get("kills");
    }

    public void setKills(long kills) {

        set("kills", kills);
    }

    public void addKill() {

        addKill(1);
    }

    public void addKill(long amount) {

        add("kills", amount);
    }

    /*
     * =========================
     * DEATHS
     * =========================
     */

    public long getDeaths() {

        return get("deaths");
    }

    public void setDeaths(long deaths) {

        set("deaths", deaths);
    }

    public void addDeath() {

        addDeath(1);
    }

    public void addDeath(long amount) {

        add("deaths", amount);
    }

    /*
     * =========================
     * WINS
     * =========================
     */

    public long getWins() {

        return get("wins");
    }

    public void setWins(long wins) {

        set("wins", wins);
    }

    public void addWin() {

        addWin(1);
    }

    public void addWin(long amount) {

        add("wins", amount);
    }

    /*
     * =========================
     * LOSSES
     * =========================
     */

    public long getLosses() {

        return get("losses");
    }

    public void setLosses(long losses) {

        set("losses", losses);
    }

    public void addLoss() {

        addLoss(1);
    }

    public void addLoss(long amount) {

        add("losses", amount);
    }

    /*
     * =========================
     * PARTIDAS
     * =========================
     */

    public long getGamesPlayed() {

        return get("games_played");
    }

    public void setGamesPlayed(long games) {

        set("games_played", games);
    }

    public void addGamePlayed() {

        addGamePlayed(1);
    }

    public void addGamePlayed(long amount) {

        add("games_played", amount);
    }

    /*
     * =========================
     * GET ALL
     * =========================
     */

    public Map<String, Long> getAll() {

        return Map.copyOf(values);
    }
}