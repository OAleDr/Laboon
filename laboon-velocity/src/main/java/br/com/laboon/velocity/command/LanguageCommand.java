package br.com.laboon.velocity.command;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageService;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;

import java.util.List;

public final class LanguageCommand
        implements SimpleCommand {

    private final AccountManager accountManager;
    private final LanguageService languageService;

    public LanguageCommand(
            AccountManager accountManager,
            LanguageService languageService
    ) {

        this.accountManager =
                accountManager;

        this.languageService =
                languageService;
    }

    @Override
    public void execute(
            Invocation invocation
    ) {

        if (!(invocation.source()
                instanceof Player player)) {

            invocation.source().sendMessage(
                    Component.text(
                            "Apenas jogadores podem utilizar este comando."
                    )
            );

            return;
        }

        String[] arguments =
                invocation.arguments();

        Account account =
                accountManager.get(
                        player.getUniqueId()
                );

        if (account == null) {

            player.sendMessage(
                    Component.text(
                            "Sua conta ainda não foi carregada."
                    )
            );

            return;
        }

        if (arguments.length == 0) {

            showLanguages(
                    player,
                    account
            );

            return;
        }

        String code =
                arguments[0];

        LanguageLocale locale;

        try {

            locale =
                    LanguageLocale.fromCode(
                            code
                    );

        } catch (IllegalArgumentException exception) {

            player.sendMessage(
                    message(
                            account,
                            "language.invalid"
                    )
            );

            return;
        }

        account
                .getPreferences()
                .setLanguage(
                        locale
                );

        accountManager.save(
                account
        );

        player.sendMessage(
                languageService.message(
                        locale,
                        "velocity",
                        "language.changed"
                )
        );
    }

    @Override
    public List<String> suggest(
            Invocation invocation
    ) {

        if (invocation.arguments().length > 1) {

            return List.of();
        }

        String input =
                invocation.arguments().length == 0
                        ? ""
                        : invocation.arguments()[0]
                        .toLowerCase();

        return List.of(
                        "pt_BR",
                        "en_US",
                        "es_ES"
                )
                .stream()
                .filter(
                        locale ->
                                locale.toLowerCase()
                                        .startsWith(input)
                )
                .toList();
    }

    private void showLanguages(
            Player player,
            Account account
    ) {

        LanguageLocale current =
                account
                        .getPreferences()
                        .getLanguage();

        player.sendMessage(
                message(
                        account,
                        "language.header"
                )
        );

        sendLanguage(
                player,
                account,
                LanguageLocale.ptBR(),
                current
        );

        sendLanguage(
                player,
                account,
                LanguageLocale.enUS(),
                current
        );

        sendLanguage(
                player,
                account,
                LanguageLocale.esES(),
                current
        );
    }

    private void sendLanguage(
            Player player,
            Account account,
            LanguageLocale locale,
            LanguageLocale current
    ) {

        String key;

        if (locale.equals(current)) {

            key =
                    "language.option.current";

        } else {

            key =
                    "language.option";
        }

        player.sendMessage(
                languageService.message(
                        current,
                        "velocity",
                        key,
                        java.util.Map.of(
                                "language",
                                getLanguageName(locale),
                                "code",
                                locale.getCode()
                        )
                )
        );
    }

    private String getLanguageName(
            LanguageLocale locale
    ) {

        return switch (
                locale.getCode()
                ) {

            case "pt_BR" ->
                    "Português (Brasil)";

            case "en_US" ->
                    "English (US)";

            case "es_ES" ->
                    "Español";

            default ->
                    locale.getCode();
        };
    }

    private Component message(
            Account account,
            String key
    ) {

        return languageService.message(
                account
                        .getPreferences()
                        .getLanguage(),
                "velocity",
                key
        );
    }
}