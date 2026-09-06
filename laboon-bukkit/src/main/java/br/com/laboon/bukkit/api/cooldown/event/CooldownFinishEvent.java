package br.com.laboon.bukkit.api.cooldown.event;

import br.com.laboon.bukkit.api.cooldown.types.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public final class CooldownFinishEvent extends CooldownEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public CooldownFinishEvent(Player player, Cooldown cooldown) {
        super(player, cooldown);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}