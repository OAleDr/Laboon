package br.com.laboon.bukkit.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public final class GuiItem {

    private final Material material;

    private int amount = 1;

    private boolean hideAttributes;

    private ItemStack baseItem;

    private Component name;

    private final List<Component> lore = new ArrayList<>();

    private GuiClickAction clickAction;

    private GuiItem(Material material) {

        if (material == null) {
            throw new IllegalArgumentException("O material não pode ser nulo.");
        }

        this.material = material;
    }

    public static GuiItem item(Material material) {

        return new GuiItem(material);
    }

    public GuiItem amount(int amount) {

        if (amount < 1) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        this.amount = amount;

        return this;
    }

    public GuiItem name(String name) {

        if (name == null) {
            this.name = null;
            return this;
        }

        this.name = MiniMessage.miniMessage().deserialize(name);

        return this;
    }

    public GuiItem name(Component name) {

        this.name = name;

        return this;
    }

    /**
     * Adiciona linhas ao lore existente.
     */
    public GuiItem lore(String... lines) {

        if (lines == null) {
            return this;
        }

        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (String line : lines) {

            if (line == null) {
                lore.add(Component.empty());
                continue;
            }

            lore.add(miniMessage.deserialize(line));
        }

        return this;
    }

    /**
     * Adiciona linhas ao lore existente.
     */
    public GuiItem lore(List<String> lines) {

        if (lines == null) {
            return this;
        }

        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (String line : lines) {

            if (line == null) {
                lore.add(Component.empty());
                continue;
            }

            lore.add(miniMessage.deserialize(line));
        }

        return this;
    }

    /**
     * Substitui completamente o lore atual.
     */
    public GuiItem loreComponents(List<Component> lines) {

        lore.clear();

        if (lines != null) {
            lore.addAll(lines);
        }

        return this;
    }

    /**
     * Remove todas as linhas do lore.
     */
    public GuiItem clearLore() {

        lore.clear();

        return this;
    }

    public GuiItem hideAttributes() {

        this.hideAttributes = true;

        return this;
    }

    public GuiItem playerHead(Player player) {

        if (player == null) {
            throw new IllegalArgumentException("O jogador não pode ser nulo.");
        }

        return playerHead((OfflinePlayer) player);
    }

    /**
     * Cria uma cabeça usando um OfflinePlayer.
     * <p>
     * Isso permite mostrar a cabeça de amigos
     * que estão offline.
     */
    public GuiItem playerHead(OfflinePlayer player) {

        if (player == null) {
            throw new IllegalArgumentException("O jogador não pode ser nulo.");
        }

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta instanceof SkullMeta meta) {

            meta.setOwningPlayer(player);

            item.setItemMeta(meta);
        }

        return itemStack(item);
    }

    public GuiItem itemStack(ItemStack item) {

        if (item == null) {
            throw new IllegalArgumentException("O ItemStack não pode ser nulo.");
        }

        this.baseItem = item.clone();

        return this;
    }

    public GuiItem onClick(GuiClickAction action) {

        this.clickAction = action;

        return this;
    }

    public Material getMaterial() {
        return material;
    }

    public GuiClickAction getClickAction() {
        return clickAction;
    }

    public ItemStack build() {

        ItemStack item = baseItem != null ? baseItem.clone() : new ItemStack(material, amount);

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        if (name != null) {
            meta.displayName(name);
        }

        if (!lore.isEmpty()) {
            meta.lore(List.copyOf(lore));
        }

        if (hideAttributes) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        item.setItemMeta(meta);

        return item;
    }
}