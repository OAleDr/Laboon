package br.com.laboon.bukkit.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ActionBarAPI {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ActionBarAPI() {
    }

    public static void send(Player player, String text) {
        if (player == null || text == null) {
            return;
        }

        player.sendActionBar(MINI_MESSAGE.deserialize(text));
    }

    public static void send(Player player, Component component) {
        if (player == null || component == null) {
            return;
        }

        player.sendActionBar(component);
    }

    public static void broadcast(String text) {
        if (text == null) {
            return;
        }

        Component component = MINI_MESSAGE.deserialize(text);

        Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(component));
    }

    public static void broadcast(Component component) {
        if (component == null) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(component));
    }
}