package br.com.laboon.bukkit.gui;

@FunctionalInterface
public interface GuiClickAction {

    void execute(GuiClickEvent event);
}