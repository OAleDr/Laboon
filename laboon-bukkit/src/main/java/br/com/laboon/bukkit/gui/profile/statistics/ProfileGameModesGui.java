package br.com.laboon.bukkit.gui.profile.statistics;

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

public final class ProfileGameModesGui {

    private final GuiManager guiManager;

    private final ProfileProvider profileProvider;

    private final LanguageService languageService;

    public ProfileGameModesGui(GuiManager guiManager, ProfileProvider profileProvider, LanguageService languageService) {

        this.guiManager = guiManager;

        this.profileProvider = profileProvider;

        this.languageService = languageService;
    }

    public void open(Player player, PlayerProfile profile, String game) {

        LanguageLocale locale = getLocale(profile);

        String title = languageService.text(locale, "paper", "statistics.game.title").replace("{game}", getGameName(game));

        Gui gui = guiManager.create(3, title);

        if (game.equalsIgnoreCase("bedwars")) {

            createBedWarsModes(gui, profile, locale);

        } else if (game.equalsIgnoreCase("skywars")) {

            createSkyWarsModes(gui, profile, locale);
        }

        createBack(gui, profile, locale);

        createClose(gui, locale);

        gui.open(player);
    }

    /*
     * =========================
     * BEDWARS
     * =========================
     */

    private void createBedWarsModes(Gui gui, PlayerProfile profile, LanguageLocale locale) {

        createMode(gui, profile, locale, "bedwars", "general", Material.RED_BED, 10);

        createMode(gui, profile, locale, "bedwars", "solo", Material.RED_BED, 12);

        createMode(gui, profile, locale, "bedwars", "doubles", Material.RED_BED, 14);

        createMode(gui, profile, locale, "bedwars", "3v3v3v3", Material.RED_BED, 16);
    }

    /*
     * =========================
     * SKYWARS
     * =========================
     */

    private void createSkyWarsModes(Gui gui, PlayerProfile profile, LanguageLocale locale) {

        createMode(gui, profile, locale, "skywars", "general", Material.ENDER_PEARL, 11);

        createMode(gui, profile, locale, "skywars", "solo", Material.ENDER_PEARL, 13);

        createMode(gui, profile, locale, "skywars", "doubles", Material.ENDER_PEARL, 15);
    }

    /*
     * =========================
     * MODO
     * =========================
     */

    private void createMode(Gui gui, PlayerProfile profile, LanguageLocale locale, String game, String mode, Material material, int slot) {

        String nameKey = "statistics." + game + ".modes." + mode + ".name";

        String loreKey = "statistics." + game + ".modes." + mode + ".lore";

        GuiItem item = GuiItem.item(material).name(languageService.message(locale, "paper", nameKey)).lore(languageService.text(locale, "paper", loreKey)).onClick(event -> new ProfileStatisticsDetailGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile, game, mode));

        gui.setItem(slot, item);
    }

    /*
     * =========================
     * VOLTAR
     * =========================
     */

    private void createBack(Gui gui, PlayerProfile profile, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.ARROW).name(languageService.message(locale, "paper", "profile.back.name")).onClick(event -> new ProfileStatisticsGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile));

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

    /*
     * =========================
     * NOME DO JOGO
     * =========================
     */

    private String getGameName(String game) {

        return switch (game.toLowerCase()) {

            case "bedwars" -> "BedWars";

            case "skywars" -> "SkyWars";

            default -> game;
        };
    }

    private LanguageLocale getLocale(PlayerProfile profile) {

        return LanguageLocale.fromCode(profile.getAccount().getPreferences().getLanguage().toString());
    }
}