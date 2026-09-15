package br.com.laboon.bukkit.gui.reports;

import br.com.laboon.bukkit.gui.AnvilGui;
import br.com.laboon.bukkit.gui.AnvilGuiManager;
import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiClickEvent;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;

import br.com.laboon.bukkit.messaging.BukkitPlayerActionService;
import br.com.laboon.core.report.Report;
import br.com.laboon.core.report.ReportManager;
import br.com.laboon.core.report.ReportStatus;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ReportActionGui {

    private final GuiManager guiManager;
    private final ReportManager reportManager;
    private final AnvilGuiManager anvilGuiManager;
    private final BukkitPlayerActionService bukkitPlayerActionService;

    public ReportActionGui(GuiManager guiManager, ReportManager reportManager, AnvilGuiManager anvilGuiManager, BukkitPlayerActionService bukkitPlayerActionService) {

        if (guiManager == null) {
            throw new IllegalArgumentException("GuiManager não pode ser nulo.");
        }

        if (reportManager == null) {
            throw new IllegalArgumentException("ReportManager não pode ser nulo.");
        }

        if (anvilGuiManager == null) {
            throw new IllegalArgumentException("AnvilGuiManager não pode ser nulo.");
        }

        if (bukkitPlayerActionService == null) {
            throw new IllegalArgumentException("BukkitPlayerActionService não pode ser nulo.");
        }

        this.guiManager = guiManager;
        this.reportManager = reportManager;
        this.anvilGuiManager = anvilGuiManager;
        this.bukkitPlayerActionService = bukkitPlayerActionService;
    }

    public void open(Player player, Report report) {

        if (player == null || report == null) {
            return;
        }

        Gui gui = guiManager.create(3, "<dark_gray>Ações do Report");

        /*
         * ========================================
         * INFORMAÇÕES DO REPORT
         * ========================================
         */

        GuiItem information = GuiItem.item(Material.PAPER).name("<aqua><bold>REPORT</bold></aqua>").lore("<gray>Denunciante: <white>" + escape(report.getReporterName()),

                "<gray>Jogador: <white>" + escape(report.getTargetName()),

                "<gray>Servidor: <yellow>" + escape(report.getServer()),

                "",

                "<gray>Motivo original:",

                "<white>" + escape(report.getReason()),

                "",

                "<gray>Status: " + getStatusColor(report.getStatus()) + getStatusName(report.getStatus()));

        gui.setItem(13, information);

        /*
         * ========================================
         * REPORT ABERTO
         * ========================================
         */

        if (report.getStatus() == ReportStatus.OPEN) {

            /*
             * ------------------------------------
             * RESOLVER
             * ------------------------------------
             */

            GuiItem resolve = GuiItem.item(Material.LIME_WOOL).name("<green><bold>RESOLVER</bold></green>").lore("<gray>Marcar este report", "<gray>como resolvido.",

                    "",

                    "<yellow>Clique para informar", "<yellow>o motivo da resolução.").onClick(event -> openDecision(event.getPlayer(), report, ReportStatus.RESOLVED));

            gui.setItem(11, resolve);

            /*
             * ------------------------------------
             * DESCARTAR
             * ------------------------------------
             */

            GuiItem dismiss = GuiItem.item(Material.RED_WOOL).name("<red><bold>DESCARTAR</bold></red>").lore("<gray>Descartar este report.",

                    "",

                    "<yellow>Clique para informar", "<yellow>o motivo do descarte.").onClick(event -> openDecision(event.getPlayer(), report, ReportStatus.DISMISSED));

            gui.setItem(15, dismiss);
        }

        /*
         * ========================================
         * REPORT RESOLVIDO OU DESCARTADO
         * ========================================
         */

        else {

            GuiItem reopen = GuiItem.item(Material.YELLOW_WOOL).name("<yellow><bold>REABRIR</bold></yellow>").lore("<gray>Reabrir este report.",

                    "",

                    "<yellow>Clique para informar", "<yellow>o motivo da reabertura.").onClick(event -> openReopenDecision(event.getPlayer(), report));

            gui.setItem(13, reopen);
        }

        /*
         * ========================================
         * VOLTAR
         * ========================================
         */

        GuiItem back = GuiItem.item(Material.ARROW).name("<yellow>Voltar").lore("<gray>Voltar para o histórico.").onClick(event -> {

            event.close();

            openHistory(event.getPlayer(), report, report.getStatus());
        });

        gui.setItem(22, back);

        /*
         * ========================================
         * FECHAR
         * ========================================
         */

        GuiItem close = GuiItem.item(Material.BARRIER).name("<red>Fechar").lore("<gray>Clique para fechar.").onClick(GuiClickEvent::close);

        gui.setItem(26, close);

        gui.open(player);
    }

    /*
     * ============================================
     * RESOLVER / DESCARTAR
     * ============================================
     */

    private void openDecision(Player player, Report report, ReportStatus status) {

        if (player == null || report == null) {
            return;
        }

        String title;

        if (status == ReportStatus.RESOLVED) {

            title = "Motivo da resolução";

        } else {

            title = "Motivo do descarte";
        }

        AnvilGui anvil = new AnvilGui(title, "", reason -> {

            String cleanReason = cleanReason(reason);

            if (cleanReason.isEmpty()) {

                player.sendMessage("§cVocê precisa informar um motivo.");

                return;
            }

            /*
             * ====================================
             * RESOLVER
             * ====================================
             */

            if (status == ReportStatus.RESOLVED) {

                reportManager.resolve(report.getId(), player.getUniqueId(), player.getName(), cleanReason);

                player.sendMessage("§a§lREPORT §8» §7Report marcado como §aresolvido§7.");
            }

            /*
             * ====================================
             * DESCARTAR
             * ====================================
             */

            else {

                reportManager.dismiss(report.getId(), player.getUniqueId(), player.getName(), cleanReason);

                player.sendMessage("§c§lREPORT §8» §7Report foi §cdescartado§7.");
            }

            /*
             * Atualiza o objeto local.
             */
            report.setStatus(status);

            /*
             * Volta para o histórico.
             */
            openHistory(player, report, status);
        });

        anvilGuiManager.open(player, anvil);
    }

    /*
     * ============================================
     * REABRIR
     * ============================================
     */

    private void openReopenDecision(Player player, Report report) {

        if (player == null || report == null) {
            return;
        }

        AnvilGui anvil = new AnvilGui("Motivo da reabertura", "", reason -> {

            String cleanReason = cleanReason(reason);

            if (cleanReason.isEmpty()) {

                player.sendMessage("§cVocê precisa informar um motivo.");

                return;
            }

            /*
             * Por enquanto o ReportManager
             * possui somente reopen(String).
             *
             * O motivo é exigido pela GUI.
             *
             * A persistência do autor e motivo
             * da reabertura será implementada
             * junto com o histórico de decisões.
             */

            reportManager.reopen(report.getId());

            /*
             * Atualiza o objeto local.
             */
            report.setStatus(ReportStatus.OPEN);

            player.sendMessage("§e§lREPORT §8» §7Report reaberto.");

            player.sendMessage("§7Motivo: §f" + cleanReason);

            /*
             * Volta para o histórico.
             */
            openHistory(player, report, ReportStatus.OPEN);
        });

        anvilGuiManager.open(player, anvil);
    }

    /*
     * ============================================
     * HISTÓRICO
     * ============================================
     */

    private void openHistory(Player player, Report report, ReportStatus status) {

        ReportHistoryGui historyGui = new ReportHistoryGui(guiManager, reportManager, anvilGuiManager, bukkitPlayerActionService);

        historyGui.open(player, report.getTargetUniqueId(), report.getTargetName(), status, 0);
    }

    /*
     * ============================================
     * STATUS
     * ============================================
     */

    private String getStatusColor(ReportStatus status) {

        return switch (status) {

            case OPEN -> "<red>";

            case RESOLVED -> "<green>";

            case DISMISSED -> "<gray>";
        };
    }

    private String getStatusName(ReportStatus status) {

        return switch (status) {

            case OPEN -> "Aberto";

            case RESOLVED -> "Resolvido";

            case DISMISSED -> "Descartado";
        };
    }

    /*
     * ============================================
     * LIMPEZA DO MOTIVO
     * ============================================
     */

    private String cleanReason(String reason) {

        if (reason == null) {
            return "";
        }

        return reason.trim();
    }

    /*
     * ============================================
     * ESCAPE MINIMESSAGE
     * ============================================
     */

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("<", "\\<").replace(">", "\\>");
    }
}