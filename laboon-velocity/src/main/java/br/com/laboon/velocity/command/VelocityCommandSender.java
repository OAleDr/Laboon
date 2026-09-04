package br.com.laboon.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public final class VelocityCommandSender {

    private final CommandSource source;

    public VelocityCommandSender(CommandSource source) {

        if (source == null) {
            throw new IllegalArgumentException(
                    "CommandSource não pode ser nulo."
            );
        }

        this.source = source;
    }

    public CommandSource getSource() {
        return source;
    }

    public boolean isPlayer() {
        return source instanceof Player;
    }

    public boolean isConsole() {
        return !isPlayer();
    }

    public Optional<Player> getPlayer() {

        if (!(source instanceof Player player)) {
            return Optional.empty();
        }

        return Optional.of(player);
    }

    public Player requirePlayer() {

        if (!(source instanceof Player player)) {
            throw new IllegalStateException(
                    "Este comando só pode ser executado por um jogador."
            );
        }

        return player;
    }

    public UUID getUniqueId() {
        return requirePlayer().getUniqueId();
    }

    public String getName() {

        if (source instanceof Player player) {
            return player.getUsername();
        }

        return "Console";
    }

    public boolean hasPermission(String permission) {

        if (permission == null || permission.isBlank()) {
            return false;
        }

        return source.hasPermission(permission);
    }

    public void sendMessage(Component message) {

        if (message == null) {
            return;
        }

        source.sendMessage(message);
    }

    public void sendMessage(String message) {

        if (message == null) {
            return;
        }

        sendMessage(Component.text(message));
    }
}