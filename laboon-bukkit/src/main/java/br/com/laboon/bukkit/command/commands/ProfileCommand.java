package br.com.laboon.bukkit.command.commands;

import br.com.laboon.bukkit.command.BukkitCommandArgs;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.bukkit.gui.profile.ProfileGui;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.language.LanguageService;
import br.com.laboon.core.profile.PlayerProfile;

public final class ProfileCommand implements CommandClass {

    private final GuiManager guiManager;
    private final ProfileProvider profileProvider;
    private final LanguageService languageService;

    public ProfileCommand(GuiManager guiManager, ProfileProvider profileProvider, LanguageService languageService) {
        this.guiManager = guiManager;
        this.profileProvider = profileProvider;
        this.languageService = languageService;
    }

    @Command(name = "profile", aliases = {"perfil"}, description = "Abre seu perfil.", usage = "/profile")
    public void execute(BukkitCommandArgs args) {

        if (!args.isPlayer()) {

            args.getSender().sendMessage("Apenas jogadores podem utilizar este comando.");

            return;
        }

        PlayerProfile profile = profileProvider.getProfile(args.getSender().getPlayer());

        if (profile == null) {

            args.getSender().sendMessage("§cSeu perfil não está disponível no momento.");

            return;
        }

        new ProfileGui(guiManager, profileProvider, languageService).open(args.getSender().getPlayer(), profile);
    }
}