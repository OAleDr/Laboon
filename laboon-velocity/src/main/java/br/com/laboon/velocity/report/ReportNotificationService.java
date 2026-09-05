package br.com.laboon.velocity.report;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.report.Report;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public final class ReportNotificationService {

    private final ProxyServer proxyServer;
    private final AccountManager accountManager;

    public ReportNotificationService(ProxyServer proxyServer, AccountManager accountManager) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.accountManager = accountManager;
    }

    public void notifyStaff(Report report) {

        if (report == null) {
            return;
        }

        for (Player player : proxyServer.getAllPlayers()) {

            Account account = accountManager.get(player.getUniqueId());

            if (account == null) {
                continue;
            }

            if (!account.getGroup().hasPermission(Group.TRIAL)) {

                continue;
            }

            sendNotification(player, report);
        }
    }

    private void sendNotification(Player player, Report report) {

        player.sendMessage(Component.text("§c§lREPORT §8» §f" + report.getReporterName() + " §7reportou §c" + report.getTargetName() + "§7."));

        player.sendMessage(Component.text("§7Motivo: §f" + report.getReason()));

        player.sendMessage(Component.text("§7Servidor: §f" + report.getServer()));

        player.sendMessage(Component.text("§8--------------------------------"));

        Component serverButton = Component.text("§a§l[ ENTRAR NO SERVIDOR ]").hoverEvent(HoverEvent.showText(Component.text("§7Entrar no servidor §f" + report.getServer() + "§7."))).clickEvent(ClickEvent.runCommand("/reportserver " + report.getId()));

        Component reportsButton = Component.text(" §e§l[ REPORTS ]").hoverEvent(HoverEvent.showText(Component.text("§7Abrir a lista de reports."))).clickEvent(ClickEvent.runCommand("/reports"));

        player.sendMessage(serverButton.append(reportsButton));

        player.sendMessage(Component.text("§8--------------------------------"));
    }
}