package br.com.laboon.velocity.language;

import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageModule;
import br.com.laboon.core.language.LanguageService;

import java.nio.file.Path;
import java.util.List;

public final class VelocityLanguage {

    private final LanguageService service;

    public VelocityLanguage(
            Path languageDirectory
    ) {

        LanguageModule module =
                new LanguageModule(
                        "velocity",
                        languageDirectory
                );

        this.service =
                br.com.laboon.core.language.LanguageBootstrap.create(
                        LanguageLocale.ptBR(),
                        List.of(module),
                        List.of(
                                LanguageLocale.ptBR(),
                                LanguageLocale.enUS(),
                                LanguageLocale.esES()
                        )
                );
    }

    public LanguageService getService() {
        return service;
    }
}