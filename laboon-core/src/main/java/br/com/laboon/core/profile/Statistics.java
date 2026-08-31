package br.com.laboon.core.profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Statistics {

    private final String game;

    private final Map<String, Long> values = new ConcurrentHashMap<>();

    public Statistics(String game) {

        if (game == null || game.isBlank()) {
            throw new IllegalArgumentException("O nome do jogo não pode ser vazio.");
        }

        this.game = game.trim().toLowerCase();
    }

    public String getGame() {
        return game;
    }

    public long get(String key) {

        return values.getOrDefault(key, 0L);
    }

    public void set(String key, long value) {

        values.put(key, value);
    }

    public void add(String key, long amount) {

        values.merge(key, amount, Long::sum);
    }

    public void remove(String key, long amount) {

        values.compute(key, (ignored, current) -> {

            long value = current == null ? 0L : current;

            return value - amount;
        });
    }

    public Map<String, Long> getAll() {

        return Map.copyOf(values);
    }
}