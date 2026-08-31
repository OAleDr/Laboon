package br.com.laboon.bukkit.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GuiManager implements Listener {

    private final Map<UUID, Gui> openGuis = new ConcurrentHashMap<>();

    public GuiManager() {
    }

    public Gui create(int rows, String title) {

        return new Gui(this, rows, title);
    }

    public void open(Player player, Gui gui) {

        openGuis.put(player.getUniqueId(), gui);

        player.openInventory(gui.getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {

            return;
        }

        Gui gui = openGuis.get(player.getUniqueId());

        if (gui == null) {
            return;
        }

        if (!event.getView().getTopInventory().equals(gui.getInventory())) {

            return;
        }

        /*
         * Bloqueia qualquer tentativa
         * de modificar a GUI.
         */
        event.setCancelled(true);

        int slot = event.getRawSlot();

        /*
         * Clique no inventário do jogador.
         */
        if (slot < 0 || slot >= gui.getInventory().getSize()) {

            return;
        }

        gui.handleClick(player, slot, event.getClick());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {

            return;
        }

        Gui gui = openGuis.get(player.getUniqueId());

        if (gui == null) {
            return;
        }

        if (!event.getView().getTopInventory().equals(gui.getInventory())) {

            return;
        }

        /*
         * Verifica se o jogador tentou
         * arrastar para algum slot da GUI.
         */
        for (int slot : event.getRawSlots()) {

            if (slot >= 0 && slot < gui.getInventory().getSize()) {

                event.setCancelled(true);

                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player player)) {

            return;
        }

        UUID uuid = player.getUniqueId();

        Gui gui = openGuis.get(uuid);

        if (gui == null) {
            return;
        }

        /*
         * Só remove se a GUI fechada
         * for a mesma que está registrada.
         */
        if (event.getView().getTopInventory().equals(gui.getInventory())) {

            openGuis.remove(uuid, gui);
        }
    }
}