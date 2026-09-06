package br.com.laboon.bukkit.api.cooldown.types;

import org.bukkit.inventory.ItemStack;

public final class ItemCooldown extends Cooldown {

    private final ItemStack item;

    private boolean selected;

    public ItemCooldown(ItemStack item, String name, long duration) {
        super(name, duration);

        if (item == null) {
            throw new IllegalArgumentException("Item do cooldown não pode ser nulo.");
        }

        this.item = item.clone();
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}