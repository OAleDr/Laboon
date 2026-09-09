package br.com.laboon.bukkit.api.item;

public enum ItemAction {

    RIGHT_CLICK_AIR,
    RIGHT_CLICK_BLOCK,

    LEFT_CLICK_AIR,
    LEFT_CLICK_BLOCK;

    public boolean isRightClick() {
        return this == RIGHT_CLICK_AIR || this == RIGHT_CLICK_BLOCK;
    }

    public boolean isLeftClick() {
        return this == LEFT_CLICK_AIR || this == LEFT_CLICK_BLOCK;
    }
}