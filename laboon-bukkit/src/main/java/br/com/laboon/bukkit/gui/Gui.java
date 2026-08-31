package br.com.laboon.bukkit.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public final class Gui {

    private final GuiManager manager;

    private final Inventory inventory;

    private final Map<Integer, GuiItem> items = new HashMap<>();

    Gui(GuiManager manager, int rows, String title) {

        if (rows < 1 || rows > 6) {

            throw new IllegalArgumentException("A GUI deve possuir entre 1 e 6 linhas.");
        }

        Component component = MiniMessage.miniMessage().deserialize(title);

        this.manager = manager;

        this.inventory = Bukkit.createInventory(null, rows * 9, component);
    }

    public Gui setItem(int slot, GuiItem item) {

        if (slot < 0 || slot >= inventory.getSize()) {

            throw new IllegalArgumentException("Slot inválido: " + slot);
        }

        items.put(slot, item);

        inventory.setItem(slot, item.build());

        return this;
    }

    public Gui removeItem(int slot) {

        items.remove(slot);

        inventory.setItem(slot, null);

        return this;
    }

    public Gui clear() {

        items.clear();

        inventory.clear();

        return this;
    }

    public GuiItem getItem(int slot) {

        return items.get(slot);
    }

    public Inventory getInventory() {

        return inventory;
    }

    public void open(Player player) {

        manager.open(player, this);
    }

    public void close(Player player) {

        player.closeInventory();
    }

    void handleClick(Player player, int slot, org.bukkit.event.inventory.ClickType clickType) {

        GuiItem item = items.get(slot);

        if (item == null) {
            return;
        }

        GuiClickAction action = item.getClickAction();

        if (action == null) {
            return;
        }

        GuiClickEvent event = new GuiClickEvent(player, this, slot, clickType);

        action.execute(event);
    }
}