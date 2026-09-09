package br.com.laboon.bukkit.api.item;

public enum ItemAction {

    RIGHT_CLICK_AIR,
    RIGHT_CLICK_BLOCK,

    LEFT_CLICK_AIR,
    LEFT_CLICK_BLOCK,

    RIGHT_CLICK_PLAYER,

    INVENTORY_LEFT_CLICK,
    INVENTORY_RIGHT_CLICK;

    public boolean isRightClick() {
        return this == RIGHT_CLICK_AIR || this == RIGHT_CLICK_BLOCK || this == INVENTORY_RIGHT_CLICK;
    }

    public boolean isLeftClick() {
        return this == LEFT_CLICK_AIR || this == LEFT_CLICK_BLOCK || this == INVENTORY_LEFT_CLICK;
    }
}