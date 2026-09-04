package br.com.laboon.bukkit.command;

import br.com.laboon.core.command.CommandArgs;
import org.bukkit.command.CommandSender;

public final class BukkitCommandArgs extends CommandArgs {

    private final BukkitCommandSender sender;

    public BukkitCommandArgs(CommandSender sender, String label, String[] arguments) {
        super(label, arguments);

        this.sender = new BukkitCommandSender(sender);
    }

    public BukkitCommandSender getSender() {
        return sender;
    }

    public boolean isPlayer() {
        return sender.isPlayer();
    }

    public void sendMessage(String s) {
        sender.sendMessage(s);
    }
}