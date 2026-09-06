package br.com.laboon.bukkit.api.npc;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface NPCClickAction {

    void onClick(Player player, NPCClickType clickType, NPC npc);
}