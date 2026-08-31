package br.com.laboon.core.language;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LanguageManager {

    private final LanguageLoader loader;

    private final Map<
            String,
            Map<String, Map<String, String>>
            > languages =
            new ConcurrentHashMap<>();

    public LanguageManager(
            LanguageLoader loader
    ) {
        this.loader = loader;
    }

    public void load(
            LanguageLocale locale,
            LanguageModule module
    ) {

        Path directory =
                module.getDirectory()
                        .resolve(
                                locale.getCode()
                        );

        if (!Files.exists(directory)) {
            return;
        }

        Map<String, String> messages =
                new ConcurrentHashMap<>();

        try {

            Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString()
                                    .endsWith(".yml")
                    )
                    .forEach(path ->
                            messages.putAll(
                                    loader.load(path)
                            )
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Não foi possível carregar o idioma "
                            + locale.getCode()
                            + " do módulo "
                            + module.getName(),
                    exception
            );
        }

        languages
                .computeIfAbsent(
                        locale.getCode(),
                        ignored ->
                                new ConcurrentHashMap<>()
                )
                .put(
                        module.getName(),
                        messages
                );
    }

    public String get(
            LanguageLocale locale,
            String module,
            String key
    ) {

        Map<String, Map<String, String>> modules =
                languages.get(
                        locale.getCode()
                );

        if (modules == null) {
            return null;
        }

        Map<String, String> messages =
                modules.get(module);

        if (messages == null) {
            return null;
        }

        return messages.get(key);
    }

    public boolean has(
            LanguageLocale locale,
            String module,
            String key
    ) {

        return get(
                locale,
                module,
                key
        ) != null;
    }

    public void unload(
            LanguageLocale locale
    ) {

        languages.remove(
                locale.getCode()
        );
    }

    public void clear() {

        languages.clear();
    }
}