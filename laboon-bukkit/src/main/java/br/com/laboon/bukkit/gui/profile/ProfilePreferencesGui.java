package br.com.laboon.bukkit.gui.profile;

import br.com.laboon.bukkit.gui.*;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.profile.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ProfilePreferencesGui {

    private final GuiManager guiManager;

    private final ProfileProvider profileProvider;

    private final LanguageService languageService;

    public ProfilePreferencesGui(GuiManager guiManager, ProfileProvider profileProvider, LanguageService languageService) {

        this.guiManager = guiManager;

        this.profileProvider = profileProvider;

        this.languageService = languageService;
    }

    public void open(Player player, PlayerProfile profile) {

        LanguageLocale locale = getLocale(profile);

        Gui gui = guiManager.create(3, languageService.text(locale, "paper", "profile.preferences.title"));

        /*
         * =========================
         * IDIOMA
         * =========================
         */

        GuiItem language = GuiItem.item(Material.BOOK).name(languageService.message(locale, "paper", "profile.preferences.language.name")).lore(languageService.text(locale, "paper", "profile.preferences.language.current").replace("{language}", getLanguageName(locale)),

                languageService.text(locale, "paper", "profile.preferences.language.lore")).onClick(event -> new ProfileLanguageGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile));

        /*
         * Slot 10
         */

        gui.setItem(10, language);

        /*
         * =========================
         * MENSAGENS PRIVADAS
         * =========================
         */

        GuiItem privateMessages = createToggleItem(locale, Material.PAPER, "private-messages", profile.getAccount().getPreferences().isPrivateMessages(),

                event -> {

                    boolean enabled = profile.getAccount().getPreferences().isPrivateMessages();

                    profile.getAccount().getPreferences().setPrivateMessages(!enabled);

                    profileProvider.save(profile);

                    open(event.getPlayer(), profile);
                });

        /*
         * Slot 12
         */

        gui.setItem(12, privateMessages);

        /*
         * =========================
         * SOLICITAÇÕES DE AMIZADE
         * =========================
         */

        GuiItem friendRequests = createToggleItem(locale, Material.EMERALD, "friend-requests", profile.getAccount().getPreferences().isFriendRequests(),

                event -> {

                    boolean enabled = profile.getAccount().getPreferences().isFriendRequests();

                    profile.getAccount().getPreferences().setFriendRequests(!enabled);

                    profileProvider.save(profile);

                    open(event.getPlayer(), profile);
                });

        /*
         * Slot 14
         */

        gui.setItem(14, friendRequests);

        /*
         * =========================
         * MENSAGENS DE ENTRADA
         * =========================
         */

        GuiItem serverJoinMessages = createToggleItem(locale, Material.BELL, "server-join-messages", profile.getAccount().getPreferences().isServerJoinMessages(),

                event -> {

                    boolean enabled = profile.getAccount().getPreferences().isServerJoinMessages();

                    profile.getAccount().getPreferences().setServerJoinMessages(!enabled);

                    profileProvider.save(profile);

                    open(event.getPlayer(), profile);
                });

        /*
         * Slot 16
         */

        gui.setItem(16, serverJoinMessages);

        /*
         * =========================
         * VOLTAR
         * =========================
         */

        GuiItem back = GuiItem.item(Material.ARROW).name(languageService.message(locale, "paper", "profile.back.name")).onClick(event -> new ProfileGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile));

        /*
         * Slot 22
         */

        gui.setItem(22, back);

        /*
         * =========================
         * FECHAR
         * =========================
         */

        GuiItem close = GuiItem.item(Material.BARRIER).name(languageService.message(locale, "paper", "profile.close.name")).onClick(GuiClickEvent::close);

        /*
         * Slot 26
         */

        gui.setItem(26, close);

        gui.open(player);
    }

    /*
     * =========================
     * TOGGLE
     * =========================
     */

    private GuiItem createToggleItem(LanguageLocale locale, Material material, String key, boolean enabled, GuiClickAction action) {

        String statusKey = enabled ? "profile.preferences." + key + ".enabled"

                : "profile.preferences." + key + ".disabled";

        return GuiItem.item(material).name(languageService.message(locale, "paper", "profile.preferences." + key + ".name")).lore(languageService.text(locale, "paper", statusKey),

                languageService.text(locale, "paper", "profile.preferences." + key + ".lore")).onClick(action);
    }

    /*
     * =========================
     * NOME DO IDIOMA
     * =========================
     */

    private String getLanguageName(LanguageLocale locale) {

        return switch (locale.getCode().toLowerCase()) {

            case "pt_br" -> "Português";

            case "en_us" -> "English";

            case "es_es" -> "Español";

            default -> locale.getCode();
        };
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