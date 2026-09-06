package br.com.laboon.bukkit.api;

import br.com.laboon.bukkit.api.cooldown.event.CooldownFinishEvent;
import br.com.laboon.bukkit.api.cooldown.event.CooldownStartEvent;
import br.com.laboon.bukkit.api.cooldown.types.Cooldown;
import br.com.laboon.bukkit.api.cooldown.types.ItemCooldown;
import br.com.laboon.bukkit.api.cooldown.types.SilentCooldown;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownAPI implements Listener {

    private static final char BAR_CHARACTER = '\u258C';

    private static final Map<UUID, List<Cooldown>> COOLDOWNS = new ConcurrentHashMap<>();

    private static BukkitTask task;

    private CooldownAPI() {
    }

    public static void initialize(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }

        if (task != null) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(new CooldownAPI(), plugin);

        task = Bukkit.getScheduler().runTaskTimer(plugin, CooldownAPI::update, 2L, 2L);
    }

    public static void addCooldown(Player player, Cooldown cooldown) {
        if (player == null || cooldown == null) {
            return;
        }

        CooldownStartEvent event = new CooldownStartEvent(player, cooldown);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        COOLDOWNS.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>()).add(cooldown);
    }

    public static boolean removeCooldown(Player player, String name) {
        if (player == null || name == null) {
            return false;
        }

        List<Cooldown> cooldowns = COOLDOWNS.get(player.getUniqueId());

        if (cooldowns == null) {
            return false;
        }

        Iterator<Cooldown> iterator = cooldowns.iterator();

        while (iterator.hasNext()) {
            Cooldown cooldown = iterator.next();

            if (cooldown.getName().equalsIgnoreCase(name)) {
                iterator.remove();

                if (cooldowns.isEmpty()) {
                    COOLDOWNS.remove(player.getUniqueId());
                }

                return true;
            }
        }

        return false;
    }

    public static boolean hasCooldown(Player player, String name) {
        if (player == null || name == null) {
            return false;
        }

        List<Cooldown> cooldowns = COOLDOWNS.get(player.getUniqueId());

        if (cooldowns == null) {
            return false;
        }

        return cooldowns.stream().anyMatch(cooldown -> cooldown.getName().equalsIgnoreCase(name));
    }

    public static void removeAllCooldowns(Player player) {
        if (player == null) {
            return;
        }

        COOLDOWNS.remove(player.getUniqueId());

        ActionBarAPI.send(player, " ");
    }

    private static void update() {

        for (Map.Entry<UUID, List<Cooldown>> entry : COOLDOWNS.entrySet()) {

            Player player = Bukkit.getPlayer(entry.getKey());

            if (player == null || !player.isOnline()) {
                COOLDOWNS.remove(entry.getKey());
                continue;
            }

            List<Cooldown> cooldowns = entry.getValue();

            Cooldown displayed = null;

            Iterator<Cooldown> iterator = cooldowns.iterator();

            while (iterator.hasNext()) {

                Cooldown cooldown = iterator.next();

                if (cooldown.expired()) {

                    iterator.remove();

                    if (!(cooldown instanceof SilentCooldown)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    }

                    Bukkit.getPluginManager().callEvent(new CooldownFinishEvent(player, cooldown));

                    continue;
                }

                if (cooldown instanceof ItemCooldown itemCooldown) {

                    ItemStack hand = player.getInventory().getItemInMainHand();

                    if (hand.getType() != Material.AIR && hand.isSimilar(itemCooldown.getItem())) {

                        itemCooldown.setSelected(true);
                        displayed = itemCooldown;
                        break;
                    }

                    continue;
                }

                displayed = cooldown;
            }

            if (displayed != null && !(displayed instanceof SilentCooldown)) {

                display(player, displayed);

            } else if (cooldowns.isEmpty()) {

                ActionBarAPI.send(player, " ");
                COOLDOWNS.remove(entry.getKey());

            } else {

                resetItemSelection(player, cooldowns);
            }
        }
    }

    private static void resetItemSelection(Player player, List<Cooldown> cooldowns) {

        for (Cooldown cooldown : cooldowns) {

            if (cooldown instanceof ItemCooldown itemCooldown && itemCooldown.isSelected()) {

                itemCooldown.setSelected(false);

                ActionBarAPI.send(player, " ");
                return;
            }
        }
    }

    private static void display(Player player, Cooldown cooldown) {

        double percentage = cooldown.getPercentage();

        double remaining = cooldown.getRemaining();

        int filled = (int) Math.ceil((percentage / 100D) * 20D);

        filled = Math.max(0, Math.min(20, filled));

        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < filled; i++) {
            bar.append("<green>").append(BAR_CHARACTER);
        }

        for (int i = filled; i < 20; i++) {
            bar.append("<red>").append(BAR_CHARACTER);
        }

        String message = "<white>" + cooldown.getName() + " " + bar + " <white>" + String.format(java.util.Locale.US, "%.1fs", remaining);

        ActionBarAPI.send(player, message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeAllCooldowns(event.getPlayer());
    }

    public static void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        COOLDOWNS.clear();
    }
}