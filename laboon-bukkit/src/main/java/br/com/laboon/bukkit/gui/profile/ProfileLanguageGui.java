package br.com.laboon.bukkit.gui.profile;

import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiClickEvent;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.bukkit.profile.ProfileProvider;

import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.profile.PlayerProfile;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

public final class ProfileLanguageGui {

    private final GuiManager guiManager;

    private final ProfileProvider profileProvider;

    private final LanguageService languageService;

    public ProfileLanguageGui(GuiManager guiManager, ProfileProvider profileProvider, LanguageService languageService) {

        this.guiManager = guiManager;

        this.profileProvider = profileProvider;

        this.languageService = languageService;
    }

    public void open(Player player, PlayerProfile profile) {

        LanguageLocale locale = getLocale(profile);

        Gui gui = guiManager.create(3, languageService.text(locale, "paper", "profile.preferences.language.title"));

        /*
         * =========================
         * PORTUGUÊS
         * =========================
         */

        createLanguageItem(gui, profile, locale, LanguageLocale.ptBR(), "Português", 11);

        /*
         * =========================
         * ENGLISH
         * =========================
         */

        createLanguageItem(gui, profile, locale, LanguageLocale.enUS(), "English", 13);

        /*
         * =========================
         * ESPAÑOL
         * =========================
         */

        createLanguageItem(gui, profile, locale, LanguageLocale.esES(), "Español", 15);

        /*
         * =========================
         * VOLTAR
         * =========================
         */

        GuiItem back = GuiItem.item(Material.ARROW).name(languageService.message(locale, "paper", "profile.back.name")).onClick(event -> new ProfilePreferencesGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile));

        gui.setItem(22, back);

        /*
         * =========================
         * FECHAR
         * =========================
         */

        GuiItem close = GuiItem.item(Material.BARRIER).name(languageService.message(locale, "paper", "profile.close.name")).onClick(GuiClickEvent::close);

        gui.setItem(26, close);

        gui.open(player);
    }

    /*
     * =========================
     * ITEM DE IDIOMA
     * =========================
     */

    private void createLanguageItem(Gui gui, PlayerProfile profile, LanguageLocale currentLocale, LanguageLocale language, String languageName, int slot) {

        boolean current = currentLocale.equals(language);

        String key = current ? "profile.preferences.language.option.current" : "profile.preferences.language.option";

        GuiItem item = GuiItem.item(Material.BOOK).name(languageService.message(currentLocale, "paper", key, Map.of("language", languageName, "code", language.getCode()))).lore(languageService.text(currentLocale, "paper", "profile.preferences.language.option.lore"));

        /*
         * Se já for o idioma atual,
         * o livro não faz nada.
         */

        if (!current) {

            item.onClick(event -> {

                profile.getAccount().getPreferences().setLanguage(language);

                /*
                 * Salva Account + Statistics
                 * no Redis.
                 */

                profileProvider.save(profile);

                /*
                 * Volta para Preferences
                 * já utilizando o novo idioma.
                 */

                new ProfilePreferencesGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile);
            });
        }

        gui.setItem(slot, item);
    }

    /*
     * =========================
     * IDIOMA ATUAL
     * =========================
     */

    private LanguageLocale getLocale(PlayerProfile profile) {

        return LanguageLocale.fromCode(profile.getAccount().getPreferences().getLanguage().toString());
    }
}