package br.com.laboon.bukkit.command.commands;

import br.com.laboon.bukkit.command.BukkitCommandArgs;
import br.com.laboon.bukkit.gui.reports.ReportListGui;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;

import java.util.List;

public final class ReportsCommand implements CommandClass {

    private final ReportListGui reportListGui;

    public ReportsCommand(ReportListGui reportListGui) {

        if (reportListGui == null) {
            throw new IllegalArgumentException("ReportListGui não pode ser nulo.");
        }

        this.reportListGui = reportListGui;
    }

    @Override
    public Completer getCompleter() {
        return args -> List.of();
    }

    @Command(name = "reports", aliases = {"reportes"}, description = "Abre os reports.", usage = "/reports", group = Group.TRIAL)
    public void execute(BukkitCommandArgs args) {

        if (!args.isPlayer()) {
            args.sendMessage("§cEste comando só pode ser usado por jogadores.");
            return;
        }

        reportListGui.open(args.getSender().getPlayer());
    }
}