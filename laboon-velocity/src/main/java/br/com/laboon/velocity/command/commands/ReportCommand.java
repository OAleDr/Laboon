package br.com.laboon.velocity.command.commands;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;
import br.com.laboon.core.report.Report;
import br.com.laboon.core.report.ReportManager;
import br.com.laboon.velocity.command.VelocityCommandArgs;
import br.com.laboon.velocity.report.ReportNotificationService;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.ArrayList;
import java.util.List;

public final class ReportCommand implements CommandClass {

    private final ProxyServer proxyServer;
    private final ReportManager reportManager;
    private final AccountManager accountManager;
    private final ReportNotificationService notificationService;

    public ReportCommand(ProxyServer proxyServer, ReportManager reportManager, AccountManager accountManager, ReportNotificationService notificationService) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (reportManager == null) {
            throw new IllegalArgumentException("ReportManager não pode ser nulo.");
        }

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        if (notificationService == null) {
            throw new IllegalArgumentException("ReportNotificationService não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.reportManager = reportManager;
        this.accountManager = accountManager;
        this.notificationService = notificationService;
    }

    @Override
    public Completer getCompleter() {

        return args -> {

            if (args.size() == 0) {
                return List.of();
            }

            if (args.size() == 1) {

                String input = args.get(0).toLowerCase();

                List<String> players = new ArrayList<>();

                for (Player player : proxyServer.getAllPlayers()) {

                    if (player.getUsername().toLowerCase().startsWith(input)) {

                        players.add(player.getUsername());
                    }
                }

                return players;
            }

            return List.of();
        };
    }

    @Command(name = "report", aliases = {"denunciar"}, description = "Reporta um jogador.", usage = "/report <jogador> <motivo>")
    public void execute(VelocityCommandArgs args) {

        if (!args.isPlayer()) {

            args.sendMessage("§cEste comando só pode ser usado por jogadores.");

            return;
        }

        if (args.size() < 2) {

            args.sendMessage("§cUso: /report <jogador> <motivo>");

            return;
        }

        Player reporter = args.getSender().getPlayer().orElse(null);

        if (reporter == null) {
            return;
        }

        String targetName = args.get(0);

        Player target = proxyServer.getPlayer(targetName).orElse(null);

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        if (target.getUniqueId().equals(reporter.getUniqueId())) {

            args.sendMessage("§cVocê não pode reportar a si mesmo.");

            return;
        }

        Account targetAccount = accountManager.get(target.getUniqueId());

        if (targetAccount == null) {

            args.sendMessage("§cNão foi possível carregar a conta do jogador.");

            return;
        }

        /*
         * Impede reports contra membros da equipe.
         */
        if (targetAccount.getGroup().hasPermission(Group.TRIAL)) {

            args.sendMessage("§cVocê não pode reportar um membro da equipe.");

            return;
        }

        StringBuilder reason = new StringBuilder();

        for (int i = 1; i < args.size(); i++) {

            if (reason.length() > 0) {
                reason.append(" ");
            }

            reason.append(args.get(i));
        }

        if (reason.length() == 0) {

            args.sendMessage("§cInforme o motivo do report.");

            return;
        }

        String server = reporter.getCurrentServer().map(connection -> connection.getServer().getServerInfo().getName()).orElse("Desconhecido");

        Report report = reportManager.create(reporter.getUniqueId(), reporter.getUsername(), target.getUniqueId(), target.getUsername(), reason.toString(), server);

        args.sendMessage("§a§lREPORT §8» §fSeu report foi enviado com sucesso.");

        args.sendMessage("§7Jogador: §f" + target.getUsername());

        args.sendMessage("§7Motivo: §f" + reason);

        notificationService.notifyStaff(report);
    }
}