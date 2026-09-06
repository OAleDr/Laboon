package br.com.laboon.bukkit.api;

import br.com.laboon.bukkit.api.bossbar.BossBarEntity;
import net.kyori.adventure.bossbar.BossBar;

public final class BossBarAPI {

    private BossBarAPI() {
    }

    public static BossBarEntity create(String title, float progress, BossBar.Color color, BossBar.Overlay overlay) {
        return new BossBarEntity(title, progress, color, overlay);
    }

}