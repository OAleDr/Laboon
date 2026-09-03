package br.com.laboon.bukkit.gui.profile;

import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiClickEvent;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.bukkit.gui.profile.statistics.ProfileStatisticsGui;
import br.com.laboon.bukkit.profile.ProfileProvider;

import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.profile.PlayerProfile;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

public final class ProfileGui {

    private final GuiManager guiManager;

    private final ProfileProvider profileProvider;

    private final LanguageService languageService;

    public ProfileGui(GuiManager guiManager, ProfileProvider profileProvider, LanguageService languageService) {

        this.guiManager = guiManager;

        this.profileProvider = profileProvider;

        this.languageService = languageService;
    }

    public void open(Player player, PlayerProfile profile) {

        LanguageLocale locale = getLocale(profile);

        Gui gui = guiManager.create(3, languageService.text(locale, "paper", "profile.title"));

        /*
         * =========================
         * PERFIL
         * =========================
         */

        createProfileItem(gui, player, profile, locale);


        /*
         * =========================
         * ESTATÍSTICAS
         * =========================
         */

        createStatisticsItem(gui, player, profile, locale);

        /*
         * =========================
         * EXPERIÊNCIA
         * =========================
         */

        createExperienceItem(gui, profile, locale);

        /*
         * =========================
         * PREFERÊNCIAS
         * =========================
         */

        createPreferencesItem(gui, profile, locale);

        /*
         * =========================
         * FECHAR
         * =========================
         */

        createCloseItem(gui, locale);

        gui.open(player);
    }

    /*
     * =========================
     * CABEÇA / PERFIL
     * =========================
     */

    private void createProfileItem(Gui gui, Player player, PlayerProfile profile, LanguageLocale locale) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {

            meta.setOwningPlayer(player);

            head.setItemMeta(meta);
        }

        GuiItem item = GuiItem.item(Material.PLAYER_HEAD).itemStack(head).name(languageService.message(locale, "paper", "profile.account.name", Map.of("name", profile.getName()))).lore(languageService.text(locale, "paper", "profile.account.rank").replace("{rank}", profile.getRank()),

                languageService.text(locale, "paper", "profile.account.uuid").replace("{uuid}", profile.getUniqueId().toString()));

        /*
         * Slot 4
         *
         * Centro da primeira linha.
         */

        gui.setItem(4, item);
    }


    /*
     * =========================
     * ESTATÍSTICAS
     * =========================
     */

    private void createStatisticsItem(Gui gui, Player player, PlayerProfile profile, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.PAPER).name(languageService.message(locale, "paper", "profile.statistics.name")).loreComponents(languageService.messages(locale, "paper", "profile.statistics.lore")).hideAttributes().onClick(event -> new ProfileStatisticsGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile));

        /*
         * Slot 12
         */

        gui.setItem(12, item);
    }

    /*
     * =========================
     * EXPERIÊNCIA
     * =========================
     */

    private void createExperienceItem(Gui gui, PlayerProfile profile, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.EXPERIENCE_BOTTLE).name(languageService.message(locale, "paper", "profile.experience.name")).lore(languageService.text(locale, "paper", "profile.experience.value").replace("{experience}", String.valueOf(profile.getExperience())));

        /*
         * Slot 14
         */

        gui.setItem(14, item);
    }

    /*
     * =========================
     * PREFERÊNCIAS
     * =========================
     */

    private void createPreferencesItem(Gui gui, PlayerProfile profile, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.COMPARATOR).name(languageService.message(locale, "paper", "profile.preferences.name")).loreComponents(languageService.messages(locale, "paper", "profile.preferences.lore")).onClick(event -> new ProfilePreferencesGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile));

        /*
         * Slot 16
         */

        gui.setItem(16, item);
    }

    /*
     * =========================
     * FECHAR
     * =========================
     */

    private void createCloseItem(Gui gui, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.BARRIER).name(languageService.message(locale, "paper", "profile.close.name")).onClick(GuiClickEvent::close);

        /*
         * Slot 26
         */

        gui.setItem(26, item);
    }

    /*
     * =========================
     * IDIOMA
     * =========================
     */

    private LanguageLocale getLocale(PlayerProfile profile) {

        return LanguageLocale.fromCode(profile.getAccount().getPreferences().getLanguage().toString());
    }
}