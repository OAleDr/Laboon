package br.com.laboon.bukkit.api.hologram;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface HologramClickAction {

    void onClick(
            Player player,
            HologramClickType clickType,
            int entityId
    );
}