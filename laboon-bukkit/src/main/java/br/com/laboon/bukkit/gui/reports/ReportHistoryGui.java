package br.com.laboon.bukkit.gui.reports;

import br.com.laboon.bukkit.gui.AnvilGuiManager;
import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.core.report.Report;
import br.com.laboon.core.report.ReportDecision;
import br.com.laboon.core.report.ReportManager;
import br.com.laboon.core.report.ReportStatus;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ReportHistoryGui {

    private static final int ITEMS_PER_PAGE = 21;

    private static final int PREVIOUS_PAGE_SLOT = 18;
    private static final int NEXT_PAGE_SLOT = 26;

    private static final int CENTER_SLOT = 22;

    private static final int BACK_SLOT = 49;
    private static final int CLOSE_SLOT = 53;

    private static final int START_SLOT = 10;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final GuiManager guiManager;
    private final ReportManager reportManager;
    private final AnvilGuiManager anvilGuiManager;

    public ReportHistoryGui(GuiManager guiManager, ReportManager reportManager, AnvilGuiManager anvilGuiManager) {

        if (guiManager == null) {
            throw new IllegalArgumentException("GuiManager não pode ser nulo.");
        }

        if (reportManager == null) {
            throw new IllegalArgumentException("ReportManager não pode ser nulo.");
        }

        if (anvilGuiManager == null) {
            throw new IllegalArgumentException("AnvilGuiManager não pode ser nulo.");
        }

        this.guiManager = guiManager;
        this.reportManager = reportManager;
        this.anvilGuiManager = anvilGuiManager;
    }

    /*
     * ==================================================
     * OPEN
     * ==================================================
     */

    public void open(Player player, UUID targetUniqueId, String targetName, ReportStatus status) {

        open(player, targetUniqueId, targetName, status, 0);
    }

    public void open(Player player, UUID targetUniqueId, String targetName, ReportStatus status, int page) {

        if (player == null) {
            return;
        }

        if (targetUniqueId == null) {
            return;
        }

        if (targetName == null || targetName.isBlank()) {
            targetName = "Desconhecido";
        }

        if (status == null) {
            status = ReportStatus.OPEN;
        }

        List<Report> reports = reportManager.findByTarget(targetUniqueId);

        reports = reports.stream().sorted(Comparator.comparing(Report::getCreatedAt).reversed()).toList();

        int totalPages = calculateTotalPages(reports.size());

        if (page < 0) {
            page = 0;
        }

        if (totalPages > 0 && page >= totalPages) {
            page = totalPages - 1;
        }

        Gui gui = guiManager.create(6, "<dark_gray>Histórico <gray>• <white>" + escape(targetName) + " <dark_gray>(" + (page + 1) + "/" + Math.max(totalPages, 1) + ")");

        buildReportItems(gui, player, reports, page);

        buildNavigation(gui, player, targetUniqueId, targetName, status, page, totalPages);

        buildBackButton(gui, player, status);

        buildCloseButton(gui);

        gui.open(player);
    }

    /*
     * ==================================================
     * REPORT ITEMS
     * ==================================================
     */

    private void buildReportItems(Gui gui, Player player, List<Report> reports, int page) {

        if (reports.isEmpty()) {

            gui.setItem(CENTER_SLOT, GuiItem.item(Material.BARRIER).name("<red>Nenhum report").lore("<gray>Este jogador ainda", "<gray>não possui reports.").onClick(event -> event.close()));

            return;
        }

        int startIndex = page * ITEMS_PER_PAGE;

        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, reports.size());

        int slot = START_SLOT;

        for (int index = startIndex; index < endIndex; index++) {

            Report report = reports.get(index);

            buildReportItem(gui, player, report, slot);

            slot++;

            /*
             * Layout:
             *
             * 10 11 12 13 14 15 16
             * 19 20 21 22 23 24 25
             * 28 29 30 31 32 33 34
             */

            if (slot == 17 || slot == 26) {
                slot += 2;
            }
        }
    }

    /*
     * ==================================================
     * REPORT ITEM
     * ==================================================
     */

    private void buildReportItem(Gui gui, Player player, Report report, int slot) {

        Material material = getStatusMaterial(report.getStatus());

        String status = getStatusName(report.getStatus());

        String date = DATE_FORMATTER.format(report.getCreatedAt().atZone(ZoneId.systemDefault()));

        /*
         * Busca o histórico de decisões
         * desse report.
         */

        List<ReportDecision> decisions = reportManager.findDecisions(report.getId());

        GuiItem item = GuiItem.item(material).name("<white>Report <gray>#" + getShortId(report));

        /*
         * ==================================================
         * INFORMAÇÕES DO REPORT
         * ==================================================
         */

        item.lore("<gray>Denunciante: <white>" + escape(report.getReporterName()),

                "",

                "<gray>Motivo do report:",

                "<white>" + escape(report.getReason()),

                "",

                "<gray>Status: " + getStatusColor(report.getStatus()) + status,

                "<gray>Servidor: <yellow>" + escape(report.getServer()),

                "<gray>Data: <white>" + date,

                "");

        /*
         * ==================================================
         * HISTÓRICO DE DECISÕES
         * ==================================================
         */

        if (decisions.isEmpty()) {

            item.lore("<dark_gray>Histórico de decisões:", "<gray>Nenhuma decisão registrada.");

        } else {

            item.lore("<dark_gray>Histórico de decisões:");

            for (ReportDecision decision : decisions) {

                item.lore("");

                item.lore(getDecisionIcon(decision.getNewStatus()) + " " + getDecisionName(decision.getNewStatus()));

                item.lore("<gray>Quem: <white>" + escape(decision.getStaffName()));

                item.lore("<gray>Motivo:");

                item.lore("<white>" + escape(decision.getReason()));

                item.lore("<gray>Data: <white>" + formatDate(decision.getCreatedAt()));
            }
        }

        /*
         * ==================================================
         * AÇÕES
         * ==================================================
         */

        item.lore("", "<yellow>Clique" + " <gray>para ver as ações.",

                "<red>Shift + clique" + " <gray>para sugerir um ban.");

        item.onClick(event -> {

            /*
             * Shift + clique:
             * sugestão de ban.
             */

            if (event.getClickType().isShiftClick()) {

                suggestBan(player, report);

                return;
            }

            /*
             * Clique normal:
             * abre o painel de ações.
             */

            ReportActionGui actionGui = new ReportActionGui(guiManager, reportManager, anvilGuiManager);

            actionGui.open(player, report);
        });

        gui.setItem(slot, item);
    }

    /*
     * ==================================================
     * NAVEGAÇÃO
     * ==================================================
     */

    private void buildNavigation(Gui gui, Player player, UUID targetUniqueId, String targetName, ReportStatus status, int page, int totalPages) {

        /*
         * Página anterior.
         */

        if (page > 0) {

            gui.setItem(PREVIOUS_PAGE_SLOT, GuiItem.item(Material.ARROW).name("<yellow>Página anterior").lore("<gray>Voltar para a página", "<gray>anterior.").onClick(event -> open(player, targetUniqueId, targetName, status, page - 1)));

        } else {

            gui.setItem(PREVIOUS_PAGE_SLOT, GuiItem.item(Material.GRAY_STAINED_GLASS_PANE).name("<dark_gray> "));
        }

        /*
         * Próxima página.
         */

        if (totalPages > 0 && page + 1 < totalPages) {

            gui.setItem(NEXT_PAGE_SLOT, GuiItem.item(Material.ARROW).name("<yellow>Próxima página").lore("<gray>Avançar para a próxima", "<gray>página.").onClick(event -> open(player, targetUniqueId, targetName, status, page + 1)));

        } else {

            gui.setItem(NEXT_PAGE_SLOT, GuiItem.item(Material.GRAY_STAINED_GLASS_PANE).name("<dark_gray> "));
        }
    }

    /*
     * ==================================================
     * VOLTAR
     * ==================================================
     */

    private void buildBackButton(Gui gui, Player player, ReportStatus status) {

        gui.setItem(BACK_SLOT, GuiItem.item(Material.ARROW).name("<yellow>Voltar").lore("<gray>Voltar para a lista", "<gray>de reports.").onClick(event -> {

            ReportListGui listGui = new ReportListGui(guiManager, reportManager, anvilGuiManager);

            listGui.open(player, status);
        }));
    }

    /*
     * ==================================================
     * FECHAR
     * ==================================================
     */

    private void buildCloseButton(Gui gui) {

        gui.setItem(CLOSE_SLOT, GuiItem.item(Material.BARRIER).name("<red>Fechar").lore("<gray>Clique para fechar.").onClick(event -> event.close()));
    }

    /*
     * ==================================================
     * BAN
     * ==================================================
     */

    private void suggestBan(Player player, Report report) {

        player.closeInventory();

        player.sendMessage("§c§lBAN §8» §7Sugestão de comando:");

        player.sendMessage("§f/ban " + report.getTargetName() + " " + report.getReason() + " <duração>");
    }

    /*
     * ==================================================
     * DECISION
     * ==================================================
     */

    private String getDecisionIcon(ReportStatus status) {

        return switch (status) {

            case OPEN -> "<yellow>↻";

            case RESOLVED -> "<green>✓";

            case DISMISSED -> "<gray>✕";
        };
    }

    private String getDecisionName(ReportStatus status) {

        return switch (status) {

            case OPEN -> "<yellow>Report reaberto";

            case RESOLVED -> "<green>Report resolvido";

            case DISMISSED -> "<gray>Report descartado";
        };
    }

    /*
     * ==================================================
     * STATUS
     * ==================================================
     */

    private Material getStatusMaterial(ReportStatus status) {

        return switch (status) {

            case OPEN -> Material.RED_DYE;

            case RESOLVED -> Material.LIME_DYE;

            case DISMISSED -> Material.GRAY_DYE;
        };
    }

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
     * ==================================================
     * DATA
     * ==================================================
     */

    private String formatDate(java.time.Instant instant) {

        if (instant == null) {
            return "Desconhecida";
        }

        return DATE_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    /*
     * ==================================================
     * ID
     * ==================================================
     */

    private String getShortId(Report report) {

        String id = report.getId();

        return id.substring(0, Math.min(8, id.length()));
    }

    /*
     * ==================================================
     * PAGINAÇÃO
     * ==================================================
     */

    private int calculateTotalPages(int size) {

        if (size <= 0) {
            return 0;
        }

        return (size + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
    }

    /*
     * ==================================================
     * MINIMESSAGE ESCAPE
     * ==================================================
     */

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("<", "\\<").replace(">", "\\>");
    }
}