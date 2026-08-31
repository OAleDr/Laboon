package br.com.laboon.bukkit.gui.profile.statistics;

import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiClickEvent;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.profile.PlayerProfile;
import br.com.laboon.core.profile.Statistics;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ProfileStatisticsDetailGui {

    private final GuiManager guiManager;

    private final ProfileProvider profileProvider;

    private final LanguageService languageService;

    public ProfileStatisticsDetailGui(GuiManager guiManager, ProfileProvider profileProvider, LanguageService languageService) {

        this.guiManager = guiManager;

        this.profileProvider = profileProvider;

        this.languageService = languageService;
    }

    public void open(Player player, PlayerProfile profile, String game, String mode) {

        LanguageLocale locale = getLocale(profile);

        Statistics statistics = profile.getStatistics(game, mode);

        String title = languageService.text(locale, "paper", "statistics.detail.title").replace("{game}", getGameName(game)).replace("{mode}", getModeName(mode));

        Gui gui = guiManager.create(3, title);

        createStatistics(gui, statistics, game, locale);

        createBack(gui, profile, game, locale);

        createClose(gui, locale);

        gui.open(player);
    }

    /*
     * =========================
     * ESTATÍSTICAS
     * =========================
     */

    private void createStatistics(Gui gui, Statistics statistics, String game, LanguageLocale locale) {

        GuiItem item = GuiItem.item(getMaterial(game)).name(languageService.message(locale, "paper", "statistics.detail.name")).lore(languageService.text(locale, "paper", "statistics.detail.wins").replace("{wins}", String.valueOf(statistics.getWins())),

                languageService.text(locale, "paper", "statistics.detail.losses").replace("{losses}", String.valueOf(statistics.getLosses())),

                languageService.text(locale, "paper", "statistics.detail.kills").replace("{kills}", String.valueOf(statistics.getKills())),

                languageService.text(locale, "paper", "statistics.detail.deaths").replace("{deaths}", String.valueOf(statistics.getDeaths())),

                "",

                languageService.text(locale, "paper", "statistics.detail.games").replace("{games}", String.valueOf(statistics.getGamesPlayed())));

        gui.setItem(13, item);
    }

    /*
     * =========================
     * VOLTAR
     * =========================
     */

    private void createBack(Gui gui, PlayerProfile profile, String game, LanguageLocale locale) {

        GuiItem item = GuiItem.item(Material.ARROW).name(languageService.message(locale, "paper", "profile.back.name")).onClick(event -> new ProfileGameModesGui(guiManager, profileProvider, languageService).open(event.getPlayer(), profile, game));

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
     * MATERIAL
     * =========================
     */

    private Material getMaterial(String game) {

        return switch (game.toLowerCase()) {

            case "bedwars" -> Material.RED_BED;

            case "skywars" -> Material.ENDER_PEARL;

            default -> Material.BOOK;
        };
    }

    /*
     * =========================
     * NOMES
     * =========================
     */

    private String getGameName(String game) {

        return switch (game.toLowerCase()) {

            case "bedwars" -> "BedWars";

            case "skywars" -> "SkyWars";

            default -> game;
        };
    }

    private String getModeName(String mode) {

        return switch (mode.toLowerCase()) {

            case "general" -> "Geral";

            case "solo" -> "Solo";

            case "doubles" -> "Duplas";

            case "3v3v3v3" -> "3v3v3v3";

            case "4v4v4v4" -> "4v4v4v4";

            default -> mode;
        };
    }

    private LanguageLocale getLocale(PlayerProfile profile) {

        return LanguageLocale.fromCode(profile.getAccount().getPreferences().getLanguage().toString());
    }
}