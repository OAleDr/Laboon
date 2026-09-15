package br.com.laboon.bukkit.display;

import br.com.laboon.bukkit.LaboonBukkit;
import br.com.laboon.core.rewards.Reward;
import br.com.laboon.core.rewards.RewardResult;
import br.com.laboon.core.rewards.RewardSource;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public final class DisplayListener implements Listener {

    private final JavaPlugin plugin;
    private final DisplayManager displayManager;

    private BukkitTask updateTask;

    public DisplayListener(JavaPlugin plugin, DisplayManager displayManager) {
        if (plugin == null) {
            throw new IllegalArgumentException("O plugin não pode ser nulo.");
        }

        if (displayManager == null) {
            throw new IllegalArgumentException("O DisplayManager não pode ser nulo.");
        }

        this.plugin = plugin;
        this.displayManager = displayManager;
    }

    public void start() {

        if (updateTask != null) {
            return;
        }

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, displayManager::updateAll, 0L, 20L);
    }

    public void stop() {

        if (updateTask == null) {
            return;
        }

        updateTask.cancel();
        updateTask = null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        displayManager.update(event.getPlayer());

        displayManager.updateAll();

        UUID uuid = event.getPlayer().getUniqueId();

        RewardResult result =
                LaboonBukkit
                        .getInstance()
                        .getRewardService()
                        .give(
                                uuid,
                                new Reward()
                                        .addCoins(100)
                                        .addTokens(5)
                                        .addExperience(250),
                                RewardSource.ADMIN,
                                "test:reward:" + uuid
                        );

        plugin.getLogger().info(
                "Reward test: " +
                        result.getStatus()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);

        displayManager.removePlayer(event.getPlayer());

        displayManager.updateAll();
    }
}