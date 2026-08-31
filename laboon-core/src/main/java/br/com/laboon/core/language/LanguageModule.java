package br.com.laboon.core.language;

import java.nio.file.Path;
import java.util.Objects;

public final class LanguageModule {

    private final String name;
    private final Path directory;

    public LanguageModule(String name, Path directory) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do módulo não pode ser vazio.");
        }

        if (directory == null) {
            throw new IllegalArgumentException("O diretório do módulo não pode ser nulo.");
        }

        this.name = name;
        this.directory = directory;
    }

    public String getName() {
        return name;
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof LanguageModule other)) {
            return false;
        }

        return name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
}