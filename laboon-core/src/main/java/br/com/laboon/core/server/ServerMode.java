package br.com.laboon.core.server;

public enum ServerMode {

    SOLO(
            "Solo",
            "Jogue sozinho contra outros jogadores."
    ),

    DOUBLES(
            "Duplas",
            "Jogue em equipes de dois jogadores."
    ),

    TEAMS(
            "Times",
            "Jogue em equipes."
    );

    private final String displayName;
    private final String description;

    ServerMode(
            String displayName,
            String description
    ) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}