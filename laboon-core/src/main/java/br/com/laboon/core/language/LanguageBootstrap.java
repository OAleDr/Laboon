package br.com.laboon.core.language;

import java.util.List;

public final class LanguageBootstrap {

    private LanguageBootstrap() {
    }

    public static LanguageService create(
            LanguageLocale defaultLocale,
            List<LanguageModule> modules,
            List<LanguageLocale> locales
    ) {

        LanguageLoader loader =
                new LanguageLoader();

        LanguageManager manager =
                new LanguageManager(
                        loader
                );

        for (LanguageModule module : modules) {

            for (LanguageLocale locale : locales) {

                manager.load(
                        locale,
                        module
                );
            }
        }

        return new LanguageService(
                manager,
                defaultLocale
        );
    }
}