package br.com.laboon.core.language;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class LanguageLoader {

    private final Yaml yaml;

    public LanguageLoader() {
        this.yaml = new Yaml();
    }

    public Map<String, String> load(Path file) {

        if (!Files.exists(file)) {
            return Map.of();
        }

        try (Reader reader = Files.newBufferedReader(file)) {

            Object data = yaml.load(reader);

            if (!(data instanceof Map<?, ?> map)) {
                return Map.of();
            }

            Map<String, String> messages = new HashMap<>();

            flatten("", map, messages);

            return messages;

        } catch (IOException exception) {

            throw new IllegalStateException("Não foi possível carregar o idioma: " + file, exception);
        }
    }

    private void flatten(String prefix, Map<?, ?> source, Map<String, String> target) {

        for (Map.Entry<?, ?> entry : source.entrySet()) {

            String key = String.valueOf(entry.getKey());

            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nested) {

                flatten(fullKey, nested, target);

                continue;
            }

            if (value != null) {

                target.put(fullKey, String.valueOf(value));
            }
        }
    }
}