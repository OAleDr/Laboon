package br.com.laboon.core.progression.prestige;

public record PrestigeDefinition(int prestige, int requiredLevel) {
    public PrestigeDefinition {
        if (prestige < 0) throw new IllegalArgumentException("Prestige não pode ser negativo.");
        if (requiredLevel < 1) throw new IllegalArgumentException("Nível necessário deve ser maior que zero.");
    }
}
