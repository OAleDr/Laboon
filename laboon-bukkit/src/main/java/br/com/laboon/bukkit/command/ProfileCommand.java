package br.com.laboon.bukkit.command;

import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.bukkit.gui.profile.ProfileGui;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.profile.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        new ProfileGui(guiManager, profileProvider, languageService).open(player, profile);

        return true;
    }
}