package br.com.laboon.bukkit.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Material;
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

        this.name = MiniMessage.miniMessage().deserialize(name);

        return this;
    }

    public GuiItem name(Component name) {

        this.name = name;

        return this;
    }

    public GuiItem lore(String... lines) {

        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (String line : lines) {

            lore.add(miniMessage.deserialize(line));
        }

        return this;
    }

    public GuiItem lore(List<String> lines) {

        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (String line : lines) {

            lore.add(miniMessage.deserialize(line));
        }

        return this;
    }

    public GuiItem loreComponents(List<Component> lines) {

        lore.clear();

        lore.addAll(lines);

        return this;
    }

    public GuiItem hideAttributes() {

        this.hideAttributes = true;

        return this;
    }

    public GuiItem playerHead(Player player) {

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(player);

        item.setItemMeta(meta);

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