package br.com.laboon.bukkit.api.npc;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface SpawnCustomizer {

    void handleSpawn(NPC npc, Player player);
}