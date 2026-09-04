package br.com.laboon.bukkit.tab;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class TabListener implements Listener {

    private final JavaPlugin plugin;
    private final BukkitTabManager tabManager;

    private BukkitTask updateTask;

    public TabListener(JavaPlugin plugin, BukkitTabManager tabManager) {

        if (plugin == null) {
            throw new IllegalArgumentException("O plugin não pode ser nulo.");
        }

        if (tabManager == null) {
            throw new IllegalArgumentException("O BukkitTabManager não pode ser nulo.");
        }

        this.plugin = plugin;
        this.tabManager = tabManager;
    }

    public void start() {

        if (updateTask != null) {
            return;
        }

        updateTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                tabManager::updateAll,
                0L,
                20L
        );
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

        tabManager.update(event.getPlayer());

        tabManager.updateAll();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {

        tabManager.removePlayer(event.getPlayer());
        tabManager.updateAll();
    }

}