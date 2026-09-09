package br.com.laboon.bukkit.api.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ClickItemListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (isEmpty(item)) {
            return;
        }

        ItemAction itemAction;

        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
                itemAction = ItemAction.RIGHT_CLICK_AIR;
                break;
            case RIGHT_CLICK_BLOCK:
                itemAction = ItemAction.RIGHT_CLICK_BLOCK;
                break;
            default:
                return;
        }

        if (ActionItemStack.handle(event.getPlayer(), item, itemAction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (isEmpty(item)) {
            return;
        }
        ItemAction itemAction;
        switch (event.getClick()) {
            case LEFT:
                itemAction = ItemAction.INVENTORY_LEFT_CLICK;
                break;
            case RIGHT:
                itemAction = ItemAction.INVENTORY_RIGHT_CLICK;
                break;
            default:
                return;
        }
        if (ActionItemStack.handle(player, item, itemAction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player player1)) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = getItemInHand(player, event.getHand());
        if (isEmpty(item)) {
            return;
        }
        if (ActionItemStack.handle(player, player1, item, ItemAction.RIGHT_CLICK_PLAYER)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof Player player1)) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (isEmpty(item)) {
            return;
        }
        if (ActionItemStack.handle(player, player1, item, ItemAction.RIGHT_CLICK_PLAYER)) {
            event.setCancelled(true);
        }
    }

    private ItemStack getItemInHand(Player player, EquipmentSlot hand) {
        if (hand == EquipmentSlot.HAND) {
            return player.getInventory().getItemInMainHand();
        }
        if (hand == EquipmentSlot.OFF_HAND) {
            return player.getInventory().getItemInOffHand();
        }
        return null;
    }

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}