package br.com.laboon.bukkit.api.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public final class BossBarEntity {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final BossBar bossBar;

    public BossBarEntity(String title, float progress, BossBar.Color color, BossBar.Overlay overlay) {
        this.bossBar = BossBar.bossBar(parse(title), clamp(progress), color, overlay);
    }

    public void addPlayer(Player player) {
        if (player == null) {
            return;
        }

        player.showBossBar(bossBar);
    }

    public void removePlayer(Player player) {
        if (player == null) {
            return;
        }

        player.hideBossBar(bossBar);
    }

    public void setTitle(String title) {
        if (title == null) {
            return;
        }

        bossBar.name(parse(title));
    }

    public void setTitle(Component component) {
        if (component == null) {
            return;
        }

        bossBar.name(component);
    }

    public void setProgress(float progress) {
        bossBar.progress(clamp(progress));
    }

    public void setColor(BossBar.Color color) {
        if (color == null) {
            return;
        }

        bossBar.color(color);
    }

    public void setOverlay(BossBar.Overlay overlay) {
        if (overlay == null) {
            return;
        }

        bossBar.overlay(overlay);
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    private static Component parse(String text) {
        return MINI_MESSAGE.deserialize(text);
    }

    private static float clamp(float progress) {
        return Math.max(0.0f, Math.min(1.0f, progress));
    }
}