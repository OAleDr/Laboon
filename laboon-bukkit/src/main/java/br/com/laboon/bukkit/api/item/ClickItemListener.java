package br.com.laboon.bukkit.api.item;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class ClickItemListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Action action = event.getAction();

        /*
         * Somente clique direito.
         */
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) {
            return;
        }

        ItemAction itemAction = action == Action.RIGHT_CLICK_AIR ? ItemAction.RIGHT_CLICK_AIR : ItemAction.RIGHT_CLICK_BLOCK;

        boolean handled = ActionItemStack.handle(event.getPlayer(), item, itemAction);

        if (handled) {
            event.setCancelled(true);
        }
    }
}