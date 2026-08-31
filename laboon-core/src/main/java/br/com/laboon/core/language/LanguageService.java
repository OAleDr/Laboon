package br.com.laboon.core.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;

public final class LanguageService {

    private final LanguageManager manager;
    private final MiniMessage miniMessage;

    private final LanguageLocale defaultLocale;

    public LanguageService(
            LanguageManager manager,
            LanguageLocale defaultLocale
    ) {

        this.manager = manager;

        this.defaultLocale = defaultLocale;

        this.miniMessage =
                MiniMessage.miniMessage();
    }

    public Component message(
            LanguageLocale locale,
            String module,
            String key
    ) {

        String text =
                resolve(
                        locale,
                        module,
                        key
                );

        return miniMessage.deserialize(
                text
        );
    }

    public Component message(
            LanguageLocale locale,
            String module,
            String key,
            Map<String, ?> placeholders
    ) {

        String text =
                resolve(
                        locale,
                        module,
                        key
                );

        for (
                Map.Entry<String, ?> entry :
                placeholders.entrySet()
        ) {

            String placeholder =
                    "{"
                            + entry.getKey()
                            + "}";

            String value =
                    String.valueOf(
                            entry.getValue()
                    );

            text =
                    text.replace(
                            placeholder,
                            value
                    );
        }

        return miniMessage.deserialize(
                text
        );
    }

    public String text(
            LanguageLocale locale,
            String module,
            String key
    ) {

        return resolve(
                locale,
                module,
                key
        );
    }

    private String resolve(
            LanguageLocale locale,
            String module,
            String key
    ) {

        String message =
                manager.get(
                        locale,
                        module,
                        key
                );

        if (message != null) {
            return message;
        }

        message =
                manager.get(
                        defaultLocale,
                        module,
                        key
                );

        if (message != null) {
            return message;
        }

        return "<red>Missing message: "
                + module
                + "."
                + key;
    }

    public LanguageLocale getDefaultLocale() {
        return defaultLocale;
    }
}