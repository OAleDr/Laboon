package br.com.laboon.bukkit.vanish;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class VanishPlayerView {

    private final VanishService vanishService;

    public VanishPlayerView(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    public List<Player> getActivePlayers() {
        return vanishService.getActivePlayers();
    }

    public List<Player> getActivePlayers(
            Collection<? extends Player> players
    ) {
        return vanishService.getNonVanishedPlayers(players);
    }

    public int getActivePlayerCount() {
        return vanishService.getActivePlayerCount();
    }

    public boolean isActive(Player player) {
        return vanishService.isActive(player);
    }
}
