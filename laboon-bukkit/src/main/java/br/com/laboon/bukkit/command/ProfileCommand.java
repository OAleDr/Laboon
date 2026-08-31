package br.com.laboon.bukkit.command;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

public final class ProfileCommand implements CommandExecutor {

    private final GuiManager guiManager;

    private final ProfileProvider profileProvider;

    private final LanguageService languageService;

    public ProfileCommand(GuiManager guiManager, ProfileProvider profileProvider, LanguageService languageService) {

        this.guiManager = guiManager;

        this.profileProvider = profileProvider;

        this.languageService = languageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage("Apenas jogadores podem utilizar este comando.");

            return true;
        }

        PlayerProfile profile = profileProvider.getProfile(player);

        if (profile == null) {

            player.sendMessage("§cSeu perfil não está disponível no momento.");

            return true;
        }

        openProfile(player, profile);

        return true;
    }

    /*
     * =========================
     * IDIOMA
     * =========================
     */

    private LanguageLocale getLocale(PlayerProfile profile) {

        return LanguageLocale.fromCode(profile.getAccount().getPreferences().getLanguage().toString());
    }

    /*
     * =========================
     * PERFIL
     * =========================
     */

    private void openProfile(Player player, PlayerProfile profile) {

        LanguageLocale locale = getLocale(profile);

        Gui gui = guiManager.create(3, languageService.text(locale, "paper", "profile.title"));

        /*
         * =========================
         * CABEÇA
         * =========================
         */

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

        skullMeta.setOwningPlayer(player);

        head.setItemMeta(skullMeta);

        GuiItem profileItem = GuiItem.item(Material.PLAYER_HEAD).itemStack(head).name(languageService.message(locale, "paper", "profile.account.name", Map.of("name", profile.getName()))).lore(languageService.text(locale, "paper", "profile.account.rank").replace("{rank}", profile.getRank()),

                languageService.text(locale, "paper", "profile.account.uuid").replace("{uuid}", player.getUniqueId().toString()));

        gui.setItem(4, profileItem);

        /*
         * =========================
         * COINS
         * =========================
         */

        GuiItem coins = GuiItem.item(Material.GOLD_INGOT).name(languageService.message(locale, "paper", "profile.coins.name")).lore(languageService.text(locale, "paper", "profile.coins.value").replace("{coins}", String.valueOf(profile.getCoins())));

        gui.setItem(11, coins);

        /*
         * =========================
         * EXPERIÊNCIA
         * =========================
         */

        GuiItem experience = GuiItem.item(Material.EXPERIENCE_BOTTLE).name(languageService.message(locale, "paper", "profile.experience.name")).lore(languageService.text(locale, "paper", "profile.experience.value").replace("{experience}", String.valueOf(profile.getExperience())));

        gui.setItem(15, experience);

        /*
         * =========================
         * ESTATÍSTICAS
         * =========================
         */

        GuiItem statistics = GuiItem.item(Material.DIAMOND_SWORD).name(languageService.message(locale, "paper", "profile.statistics.name")).loreComponents(languageService.messages(locale, "paper", "profile.statistics.lore")).hideAttributes().onClick(event -> openStatistics(event.getPlayer(), profile));

        gui.setItem(13, statistics);

        /*
         * =========================
         * PREFERÊNCIAS
         * =========================
         */

        GuiItem preferences = GuiItem.item(Material.COMPARATOR).name(languageService.message(locale, "paper", "profile.preferences.name")).loreComponents(languageService.messages(locale, "paper", "profile.preferences.lore"));

        gui.setItem(22, preferences);

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
     * ESTATÍSTICAS
     * =========================
     */

    private void openStatistics(Player player, PlayerProfile profile) {

        LanguageLocale locale = getLocale(profile);

        Gui gui = guiManager.create(3, languageService.text(locale, "paper", "statistics.title"));

        Statistics bedwars = profile.getStatistics("bedwars");

        Statistics skywars = profile.getStatistics("skywars");

        /*
         * =========================
         * BEDWARS
         * =========================
         */

        GuiItem bedwarsItem = GuiItem.item(Material.RED_BED).name(languageService.message(locale, "paper", "statistics.bedwars.name")).lore(languageService.text(locale, "paper", "statistics.bedwars.wins").replace("{wins}", String.valueOf(bedwars.getWins())),

                languageService.text(locale, "paper", "statistics.bedwars.losses").replace("{losses}", String.valueOf(bedwars.getLosses())),

                languageService.text(locale, "paper", "statistics.bedwars.kills").replace("{kills}", String.valueOf(bedwars.getKills())),

                languageService.text(locale, "paper", "statistics.bedwars.deaths").replace("{deaths}", String.valueOf(bedwars.getDeaths())),

                "",

                languageService.text(locale, "paper", "statistics.bedwars.games").replace("{games}", String.valueOf(bedwars.getGamesPlayed())));

        gui.setItem(11, bedwarsItem);

        /*
         * =========================
         * SKYWARS
         * =========================
         */

        GuiItem skywarsItem = GuiItem.item(Material.ENDER_PEARL).name(languageService.message(locale, "paper", "statistics.skywars.name")).lore(languageService.text(locale, "paper", "statistics.skywars.wins").replace("{wins}", String.valueOf(skywars.getWins())),

                languageService.text(locale, "paper", "statistics.skywars.losses").replace("{losses}", String.valueOf(skywars.getLosses())),

                languageService.text(locale, "paper", "statistics.skywars.kills").replace("{kills}", String.valueOf(skywars.getKills())),

                languageService.text(locale, "paper", "statistics.skywars.deaths").replace("{deaths}", String.valueOf(skywars.getDeaths())),

                "",

                languageService.text(locale, "paper", "statistics.skywars.games").replace("{games}", String.valueOf(skywars.getGamesPlayed())));

        gui.setItem(15, skywarsItem);

        /*
         * =========================
         * VOLTAR
         * =========================
         */

        GuiItem back = GuiItem.item(Material.ARROW).name(languageService.message(locale, "paper", "profile.back.name")).onClick(event -> openProfile(event.getPlayer(), profile));

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
}