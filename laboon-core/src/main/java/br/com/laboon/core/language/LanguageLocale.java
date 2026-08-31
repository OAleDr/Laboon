package br.com.laboon.core.language;

import java.util.Objects;

public final class LanguageLocale {

    private final String code;

    private LanguageLocale(String code) {

        this.code = code;
    }

    public static LanguageLocale of(String code) {

        if (code == null || code.isBlank()) {

            throw new IllegalArgumentException("O código do idioma não pode ser vazio.");
        }

        return new LanguageLocale(code.trim().replace('-', '_'));
    }

    public static LanguageLocale ptBR() {
        return of("pt_BR");
    }

    public static LanguageLocale enUS() {
        return of("en_US");
    }

    public static LanguageLocale esES() {
        return of("es_ES");
    }

    public String getCode() {
        return code;
    }

    public java.util.Locale toJavaLocale() {

        String[] parts = code.split("_", 2);

        if (parts.length == 1) {

            return new java.util.Locale(parts[0]);
        }

        return new java.util.Locale(parts[0], parts[1]);
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof LanguageLocale other)) {
            return false;
        }

        return code.equalsIgnoreCase(other.code);
    }

    @Override
    public int hashCode() {

        return Objects.hash(code.toLowerCase());
    }
}