package br.com.laboon.core.game;

import java.util.Objects;

public final class GameMode {

    private final String id;

    public GameMode(String id) {

        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException("O identificador do modo não pode ser vazio.");
        }

        this.id = id.trim().toLowerCase();
    }

    public String getId() {

        return id;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof GameMode other)) {
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