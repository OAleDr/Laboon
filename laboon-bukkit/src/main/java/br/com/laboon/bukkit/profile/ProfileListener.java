package br.com.laboon.bukkit.profile;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.account.group.TemporaryGroupExpirationService;
import br.com.laboon.core.profile.PlayerProfile;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.List;

public final class ProfileListener implements Listener {

    private final JavaPlugin plugin;
    private final ProfileProvider profileProvider;
    private final AccountManager accountManager;
    private final TemporaryGroupExpirationService expirationService;

    private BukkitTask expirationTask;

    public ProfileListener(JavaPlugin plugin, ProfileProvider profileProvider, AccountManager accountManager) {

        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }

        if (profileProvider == null) {
            throw new IllegalArgumentException("ProfileProvider não pode ser nulo.");
        }

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        this.plugin = plugin;
        this.profileProvider = profileProvider;
        this.accountManager = accountManager;

        this.expirationService = new TemporaryGroupExpirationService(accountManager);
    }

    public void start() {

        if (expirationTask != null) {
            return;
        }

        expirationTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkExpiredGroups, 20L, 20L * 60L);
    }

    public void stop() {

        if (expirationTask == null) {
            return;
        }

        expirationTask.cancel();
        expirationTask = null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        PlayerProfile profile = profileProvider.getProfile(player);

        if (profile == null) {
            return;
        }

        Account account = profile.getAccount();

        /*
         * Remove grupos que já expiraram enquanto
         * o jogador estava offline.
         */
        expirationService.removeExpiredGroups(account);

        /*
         * Verifica se existe algum grupo temporário
         * dentro da janela de 5 dias.
         */
        sendExpirationWarnings(player, account);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        PlayerProfile profile = profileProvider.getProfile(event.getPlayer());

        if (profile == null) {
            return;
        }

        profileProvider.save(profile);

        profileProvider.unload(event.getPlayer().getUniqueId());
    }

    private void checkExpiredGroups() {

        for (Player player : plugin.getServer().getOnlinePlayers()) {

            PlayerProfile profile = profileProvider.getProfile(player);

            if (profile == null) {
                continue;
            }

            Account account = profile.getAccount();

            expirationService.removeExpiredGroups(account);
        }
    }

    private void sendExpirationWarnings(Player player, Account account) {

        List<TemporaryGroupExpirationService.TemporaryGroupExpiration> expiringGroups = expirationService.getExpiringGroups(account);

        for (TemporaryGroupExpirationService.TemporaryGroupExpiration expiration : expiringGroups) {

            player.sendMessage("§e§lLABOON §8» §fSeu grupo §d" + getGroupName(expiration.group()) + " §fexpira em §e" + formatRemaining(expiration.remaining()) + "§f.");
        }
    }

    private String getGroupName(Group group) {

        if (group == null) {
            return "";
        }

        if (group.getDisplayName() != null && !group.getDisplayName().isBlank()) {

            return group.getDisplayName();
        }

        return group.name();
    }

    private String formatRemaining(Duration duration) {

        long seconds = Math.max(0, duration.getSeconds());

        long days = seconds / 86_400;

        seconds %= 86_400;

        long hours = seconds / 3_600;

        seconds %= 3_600;

        long minutes = seconds / 60;

        if (days > 0) {

            if (hours > 0) {

                return days + (days == 1 ? " dia e " : " dias e ") + hours + (hours == 1 ? " hora" : " horas");
            }

            return days + (days == 1 ? " dia" : " dias");
        }

        if (hours > 0) {

            if (minutes > 0) {

                return hours + (hours == 1 ? " hora e " : " horas e ") + minutes + (minutes == 1 ? " minuto" : " minutos");
            }

            return hours + (hours == 1 ? " hora" : " horas");
        }

        if (minutes > 0) {

            return minutes + (minutes == 1 ? " minuto" : " minutos");
        }

        return "menos de 1 minuto";
    }
}