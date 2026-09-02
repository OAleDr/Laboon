package br.com.laboon.core.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Game {

    private final String id;

    private final List<GameMode> modes;

    public Game(String id, List<GameMode> modes) {

        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException("O identificador do jogo não pode ser vazio.");
        }

        if (modes == null) {

            throw new IllegalArgumentException("A lista de modos não pode ser nula.");
        }

        this.id = id.trim().toLowerCase();

        this.modes = List.copyOf(new ArrayList<>(modes));
    }

    public String getId() {

        return id;
    }

    public List<GameMode> getModes() {

        return modes;
    }

    public boolean hasModes() {

        return !modes.isEmpty();
    }

    public boolean hasMode(String modeId) {

        if (modeId == null || modeId.isBlank()) {
            return false;
        }

        return modes.stream().anyMatch(mode -> mode.getId().equalsIgnoreCase(modeId));
    }

    public GameMode getMode(String modeId) {

        if (modeId == null || modeId.isBlank()) {
            return null;
        }

        return modes.stream().filter(mode -> mode.getId().equalsIgnoreCase(modeId)).findFirst().orElse(null);
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof Game other)) {
            return false;
        }

        return id.equalsIgnoreCase(other.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id.toLowerCase());
    }

    @Override
    public String toString() {

        return id;
    }
}