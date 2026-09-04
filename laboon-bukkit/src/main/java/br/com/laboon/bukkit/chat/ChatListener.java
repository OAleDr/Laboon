package br.com.laboon.bukkit.chat;

import io.papermc.paper.event.player.AsyncChatEvent;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatListener implements Listener {

    private final JavaPlugin plugin;
    private final ChatFormatter chatFormatter;

    public ChatListener(JavaPlugin plugin, ChatFormatter chatFormatter) {

        if (plugin == null) {
            throw new IllegalArgumentException("O plugin não pode ser nulo.");
        }

        if (chatFormatter == null) {
            throw new IllegalArgumentException("O ChatFormatter não pode ser nulo.");
        }

        this.plugin = plugin;
        this.chatFormatter = chatFormatter;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncChatEvent event) {

        event.setCancelled(true);

        Player player = event.getPlayer();

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        var formattedMessage = chatFormatter.format(player, message);

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcast(formattedMessage));
    }
}