package br.com.laboon.bukkit.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class BukkitCommandSender {

    private final CommandSender sender;

    public BukkitCommandSender(CommandSender sender) {
        if (sender == null) {
            throw new IllegalArgumentException("O CommandSender não pode ser nulo.");
        }

        this.sender = sender;
    }

    public CommandSender getSender() {
        return sender;
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player getPlayer() {
        if (!isPlayer()) {
            return null;
        }

        return (Player) sender;
    }

    public UUID getUniqueId() {
        Player player = getPlayer();

        return player == null ? null : player.getUniqueId();
    }

    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return true;
        }

        return sender.hasPermission(permission);
    }
}