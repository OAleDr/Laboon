package br.com.laboon.bukkit.gui.profile.statistics;

import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiClickEvent;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.bukkit.gui.profile.ProfileGui;
import br.com.laboon.bukkit.profile.ProfileProvider;

import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.profile.PlayerProfile;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ProfileStatisticsGui {

    private final GuiManager guiManager;

    private final ProfileProvider profileProvider;

    private final LanguageService languageService;

    public ProfileStatisticsGui(GuiManager guiManager, ProfileProvider profileProvider, LanguageService languageService) {

        this.guiManager = guiManager;

        this.profileProvider = profileProvider;

        this.languageService = languageService;
    }

    public void open(Player player, PlayerProfile profile) {

        LanguageLocale locale = getLocale(profile);

        Gui gui = guiManager.create(3, languageService.text(locale, "paper", "statistics.title"));

        createBedWars(gui, profile, locale);

        createSkyWars(gui, profile, locale);

        createBack(gui, profile, locale);

        createClose(gui, locale);

        gui.open(player);
    }

    /*
     * =========================
     * BEDWARS
     * =========================
     */

    private void createBedWars(Gui gui, PlayerProfile profile, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.RED_BED).name(languageService.message(locale, "paper", "statistics.bedwars.name")).lore(languageService.text(locale, "paper", "statistics.bedwars.description")).onClick(event -> new ProfileGameModesGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile, "bedwars"));

        gui.setItem(11, item);
    }

    /*
     * =========================
     * SKYWARS
     * =========================
     */

    private void createSkyWars(Gui gui, PlayerProfile profile, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.ENDER_PEARL).name(languageService.message(locale, "paper", "statistics.skywars.name")).lore(languageService.text(locale, "paper", "statistics.skywars.description")).onClick(event -> new ProfileGameModesGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile, "skywars"));

        gui.setItem(15, item);
    }

    /*
     * =========================
     * VOLTAR
     * =========================
     */

    private void createBack(Gui gui, PlayerProfile profile, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.ARROW).name(languageService.message(locale, "paper", "profile.back.name")).onClick(event -> new ProfileGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile));

        gui.setItem(22, item);
    }

    /*
     * =========================
     * FECHAR
     * =========================
     */

    private void createClose(Gui gui, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.BARRIER).name(languageService.message(locale, "paper", "profile.close.name")).onClick(GuiClickEvent::close);

        gui.setItem(26, item);
    }

    private LanguageLocale getLocale(PlayerProfile profile) {

        return LanguageLocale.fromCode(profile.getAccount().getPreferences().getLanguage().toString());
    }
}