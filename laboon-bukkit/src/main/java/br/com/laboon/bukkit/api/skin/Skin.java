package br.com.laboon.bukkit.api.skin;

import java.util.UUID;

public final class Skin {

    private final UUID uniqueId;
    private final String name;
    private final String value;
    private final String signature;

    public Skin(UUID uniqueId, String name, String value, String signature) {
        if (uniqueId == null) {
            throw new IllegalArgumentException("UUID da skin não pode ser nulo.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome da skin não pode ser nulo ou vazio.");
        }

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Valor da textura não pode ser nulo ou vazio.");
        }

        this.uniqueId = uniqueId;
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public boolean hasSignature() {
        return signature != null && !signature.isBlank();
    }

    @Override
    public String toString() {
        return "Skin{" + "uniqueId=" + uniqueId + ", name='" + name + '\'' + ", hasSignature=" + hasSignature() + '}';
    }
}