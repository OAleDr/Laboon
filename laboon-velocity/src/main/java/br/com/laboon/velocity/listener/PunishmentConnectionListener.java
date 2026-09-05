package br.com.laboon.velocity.listener;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.punishment.Ban;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class PunishmentConnectionListener {

    private final AccountManager accountManager;

    public PunishmentConnectionListener(AccountManager accountManager) {

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        this.accountManager = accountManager;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {

        Player player = event.getPlayer();

        Account account = accountManager.get(player.getUniqueId());

        if (account == null) {
            return;
        }

        Ban ban = account.getCurrentBan();

        if (ban == null) {
            return;
        }

        String duration;
        if (ban.isPermanent()) {
            duration = "Permanente";
        } else {
            duration = formatDuration(ban.getRemaining());
        }

        event.setResult(LoginEvent.ComponentResult.denied(Component.text("Você está banido da rede Laboon.\n\n" + "Motivo: " + ban.getReason() + "\n" + "Duração: " + duration)));
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