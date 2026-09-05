package br.com.laboon.bukkit.listener;

import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.account.punishment.Mute;
import br.com.laboon.core.profile.PlayerProfile;

import io.papermc.paper.event.player.AsyncChatEvent;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class PunishmentChatListener implements Listener {

    private final ProfileProvider profileProvider;

    public PunishmentChatListener(ProfileProvider profileProvider) {

        if (profileProvider == null) {
            throw new IllegalArgumentException("ProfileProvider não pode ser nulo.");
        }

        this.profileProvider = profileProvider;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {

        Player player = event.getPlayer();

        PlayerProfile profile = profileProvider.getProfile(player);

        if (profile == null) {
            return;
        }

        Mute mute = profile.getAccount().getCurrentMute();

        if (mute == null) {
            return;
        }

        event.setCancelled(true);

        String duration;

        if (mute.isPermanent()) {

            duration = "Permanente";

        } else {

            duration = formatDuration(mute.getRemaining());
        }

        player.sendMessage(Component.text("Você está mutado.\n\n" + "Motivo: " + mute.getReason() + "\n" + "Tempo restante: " + duration));
    }

    private String formatDuration(Duration duration) {

        if (duration == null) {
            return "Permanente";
        }

        long seconds = Math.max(0, duration.getSeconds());

        long days = seconds / 86_400;
        seconds %= 86_400;

        long hours = seconds / 3_600;
        seconds %= 3_600;

        long minutes = seconds / 60;
        seconds %= 60;

        List<String> parts = new ArrayList<>();

        if (days > 0) {
            parts.add(days + (days == 1 ? " dia" : " dias"));
        }

        if (hours > 0) {
            parts.add(hours + (hours == 1 ? " hora" : " horas"));
        }

        if (minutes > 0) {
            parts.add(minutes + (minutes == 1 ? " minuto" : " minutos"));
        }

        if (seconds > 0) {
            parts.add(seconds + (seconds == 1 ? " segundo" : " segundos"));
        }

        if (parts.isEmpty()) {
            return "menos de 1 segundo";
        }

        if (parts.size() == 1) {
            return parts.get(0);
        }

        if (parts.size() == 2) {
            return parts.get(0) + " e " + parts.get(1);
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.size(); i++) {

            if (i > 0) {

                if (i == parts.size() - 1) {
                    result.append(" e ");
                } else {
                    result.append(", ");
                }
            }

            result.append(parts.get(i));
        }

        return result.toString();
    }
}