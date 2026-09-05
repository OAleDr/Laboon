package br.com.laboon.velocity.command.commands;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;
import br.com.laboon.core.report.Report;
import br.com.laboon.core.report.ReportManager;
import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.velocity.command.VelocityCommandArgs;
import br.com.laboon.velocity.server.ServerConnectionService;
import br.com.laboon.velocity.server.ServerSelector;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.List;

public final class ReportServerCommand implements CommandClass {

    private final ProxyServer proxyServer;
    private final ReportManager reportManager;
    private final AccountManager accountManager;
    private final ServerSelector serverSelector;
    private final ServerConnectionService connectionService;

    public ReportServerCommand(ProxyServer proxyServer, ReportManager reportManager, AccountManager accountManager, ServerSelector serverSelector, ServerConnectionService connectionService) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (reportManager == null) {
            throw new IllegalArgumentException("ReportManager não pode ser nulo.");
        }

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        if (serverSelector == null) {
            throw new IllegalArgumentException("ServerSelector não pode ser nulo.");
        }

        if (connectionService == null) {
            throw new IllegalArgumentException("ServerConnectionService não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.reportManager = reportManager;
        this.accountManager = accountManager;
        this.serverSelector = serverSelector;
        this.connectionService = connectionService;
    }

    @Override
    public Completer getCompleter() {

        return args -> List.of();
    }

    @Command(name = "reportserver", description = "Entra no servidor de um report.", usage = "/reportserver <id>", group = Group.TRIAL)
    public void execute(VelocityCommandArgs args) {

        if (!args.isPlayer()) {
            args.sendMessage("§cEste comando só pode ser usado por jogadores.");

            return;
        }

        if (args.size() < 1) {

            args.sendMessage("§cUso: /reportserver <id>");

            return;
        }

        Player player = args.getSender().getPlayer().orElse(null);

        if (player == null) {
            return;
        }

        Account account = accountManager.get(player.getUniqueId());

        if (account == null || !account.getGroup().hasPermission(Group.TRIAL)) {

            args.sendMessage("§cVocê não possui permissão.");

            return;
        }

        String reportId = args.getArgument(0);

        Report report = reportManager.find(reportId);

        if (report == null) {

            args.sendMessage("§cReport não encontrado.");

            return;
        }

        ServerInfo server = serverSelector.find(report.getServer());

        if (server == null) {

            args.sendMessage("§cO servidor do report não está disponível.");

            return;
        }

        connectionService.connect(player, server, () -> player.sendMessage(Component.text("§cNão foi possível conectar ao servidor §f" + report.getServer() + "§c.")));
    }
}