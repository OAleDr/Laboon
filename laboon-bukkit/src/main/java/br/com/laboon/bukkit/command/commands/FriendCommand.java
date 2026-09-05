package br.com.laboon.bukkit.command.commands;

import br.com.laboon.bukkit.command.BukkitCommandArgs;
import br.com.laboon.bukkit.gui.friend.FriendGui;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;

public final class FriendCommand implements CommandClass {

    private final FriendGui friendGui;

    public FriendCommand(FriendGui friendGui) {
        this.friendGui = friendGui;
    }

    @Command(name = "amigo", aliases = {"amigos", "friend", "friends"}, description = "Abre sua lista de amigos.", usage = "/amigo")
    public void execute(BukkitCommandArgs args) {

        if (!args.isPlayer()) {

            args.getSender().sendMessage("Apenas jogadores podem utilizar este comando.");

            return;
        }

        friendGui.open(args.getSender().getPlayer());
    }
}