package br.com.laboon.bukkit.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class GuiClickEvent {

    private final Player player;
    private final Gui gui;
    private final int slot;
    private final ClickType clickType;

    public GuiClickEvent(Player player, Gui gui, int slot, ClickType clickType) {

        this.player = player;
        this.gui = gui;
        this.slot = slot;
        this.clickType = clickType;
    }

    public Player getPlayer() {
        return player;
    }

    public Gui getGui() {
        return gui;
    }

    public int getSlot() {
        return slot;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public void close() {

        player.closeInventory();
    }
}