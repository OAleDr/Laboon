package br.com.laboon.bukkit.command;

import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GuiTestCommand
        implements CommandExecutor {

    private final GuiManager guiManager;

    public GuiTestCommand(
            GuiManager guiManager
    ) {

        this.guiManager =
                guiManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    "Apenas jogadores podem usar este comando."
            );

            return true;
        }

        Gui gui =
                guiManager.create(
                        3,
                        "<gold><bold>Laboon GUI"
                );

        GuiItem diamond =
                GuiItem
                        .item(
                                Material.DIAMOND
                        )
                        .name(
                                "<aqua><bold>Diamante"
                        )
                        .lore(
                                "<gray>Clique neste item!",
                                "",
                                "<yellow>Teste da API GUI"
                        )
                        .onClick(
                                event -> {

                                    event.getPlayer()
                                            .sendMessage(
                                                    "§aFuncionou! Você clicou no diamante."
                                            );
                                }
                        );

        gui.setItem(
                13,
                diamond
        );

        gui.open(player);

        return true;
    }
}