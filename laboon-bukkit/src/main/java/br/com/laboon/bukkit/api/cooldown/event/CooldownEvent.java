package br.com.laboon.bukkit.api.cooldown.event;

import br.com.laboon.bukkit.api.cooldown.types.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class CooldownEvent extends Event {

    private final Player player;
    private final Cooldown cooldown;

    protected CooldownEvent(Player player, Cooldown cooldown) {
        if (player == null) {
            throw new IllegalArgumentException("Player não pode ser nulo.");
        }

        if (cooldown == null) {
            throw new IllegalArgumentException("Cooldown não pode ser nulo.");
        }

        this.player = player;
        this.cooldown = cooldown;
    }

    public Player getPlayer() {
        return player;
    }

    public Cooldown getCooldown() {
        return cooldown;
    }
}