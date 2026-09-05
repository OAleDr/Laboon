package br.com.laboon.bukkit.gui;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.function.Consumer;

public final class AnvilGui {

    private final String title;
    private final String initialText;
    private final Consumer<String> consumer;

    private UUID playerUniqueId;
    private Inventory inventory;

    public AnvilGui(String title, String initialText, Consumer<String> consumer) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Título da AnvilGui não pode ser vazio.");
        }

        if (initialText == null) {
            initialText = "";
        }

        if (consumer == null) {
            throw new IllegalArgumentException("Consumer da AnvilGui não pode ser nulo.");
        }

        this.title = title;
        this.initialText = initialText;
        this.consumer = consumer;
    }

    public void open(Player player) {

        if (player == null) {
            return;
        }

        this.playerUniqueId = player.getUniqueId();

        this.inventory = Bukkit.createInventory(null, InventoryType.ANVIL, Component.text(title));

        /*
         * Item utilizado como entrada da bigorna.
         */
        ItemStack item = new ItemStack(org.bukkit.Material.PAPER);

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            meta.displayName(Component.text(initialText));

            item.setItemMeta(meta);
        }

        /*
         * Slot esquerdo da bigorna.
         */
        inventory.setItem(0, item);

        player.openInventory(inventory);
    }

    public boolean isOwner(Player player) {

        if (player == null || playerUniqueId == null) {
            return false;
        }

        return playerUniqueId.equals(player.getUniqueId());
    }

    public Inventory getInventory() {
        return inventory;
    }

    public UUID getPlayerUniqueId() {
        return playerUniqueId;
    }

    public String getTitle() {
        return title;
    }

    public String getInitialText() {
        return initialText;
    }

    public Consumer<String> getConsumer() {
        return consumer;
    }
}