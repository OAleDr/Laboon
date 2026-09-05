package br.com.laboon.bukkit.gui.reports;

import br.com.laboon.bukkit.gui.AnvilGuiManager;
import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiClickEvent;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.core.report.Report;
import br.com.laboon.core.report.ReportManager;
import br.com.laboon.core.report.ReportStatus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ReportListGui {

    private static final int ITEMS_PER_PAGE = 21;

    private static final int PREVIOUS_PAGE_SLOT = 18;
    private static final int NEXT_PAGE_SLOT = 26;

    private static final int CENTER_SLOT = 22;

    private static final int CURRENT_FILTER_SLOT = 49;
    private static final int FILTER_SLOT = 50;

    private static final int CLOSE_SLOT = 53;

    private static final int START_SLOT = 10;
    private static final int ITEMS_PER_ROW = 7;

    private final GuiManager guiManager;
    private final ReportManager reportManager;
    private final AnvilGuiManager anvilGuiManager;

    public ReportListGui(GuiManager guiManager, ReportManager reportManager, AnvilGuiManager anvilGuiManager) {

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

    public void open(Player player) {

        open(player, ReportStatus.OPEN, 0);
    }

    public void open(Player player, ReportStatus status) {

        open(player, status, 0);
    }

    public void open(Player player, ReportStatus status, int page) {

        if (player == null) {
            return;
        }

        if (status == null) {
            status = ReportStatus.OPEN;
        }

        List<ReportGroup> groups = createGroups(status);

        int totalPages = calculateTotalPages(groups.size());

        if (page < 0) {
            page = 0;
        }

        if (totalPages > 0 && page >= totalPages) {
            page = totalPages - 1;
        }

        Gui gui = guiManager.create(6, "<dark_gray>Reports <gray>• <white>" + getStatusName(status) + " <dark_gray>(" + (page + 1) + "/" + Math.max(totalPages, 1) + ")");

        buildItems(gui, player, groups, status, page, totalPages);

        buildNavigation(gui, player, status, page, totalPages);

        buildFilter(gui, player, status, page);

        buildCloseButton(gui);

        gui.open(player);
    }

    private List<ReportGroup> createGroups(ReportStatus status) {

        List<Report> reports = reportManager.findAll();

        Map<UUID, ReportGroup> grouped = new LinkedHashMap<>();

        for (Report report : reports) {

            if (report == null) {
                continue;
            }

            ReportGroup group = grouped.computeIfAbsent(report.getTargetUniqueId(), uuid -> new ReportGroup(report.getTargetUniqueId(), report.getTargetName()));

            group.add(report);
        }

        List<ReportGroup> result = new ArrayList<>();

        for (ReportGroup group : grouped.values()) {

            if (group.getReportsByStatus(status).isEmpty()) {
                continue;
            }

            result.add(group);
        }

        result.sort(Comparator.comparing(ReportGroup::getLatestReportDate).reversed());

        return result;
    }

    private void buildItems(Gui gui, Player player, List<ReportGroup> groups, ReportStatus status, int page, int totalPages) {

        if (groups.isEmpty()) {

            gui.setItem(CENTER_SLOT, GuiItem.item(Material.BARRIER).name("<red>Nenhum report").lore("<gray>Nenhum jogador possui", "<gray>reports neste filtro.").onClick(event -> event.close()));

            return;
        }

        int startIndex = page * ITEMS_PER_PAGE;

        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, groups.size());

        int slot = START_SLOT;

        for (int index = startIndex; index < endIndex; index++) {

            ReportGroup group = groups.get(index);

            setReportPlayerItem(gui, player, group, status, slot);

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

    private void setReportPlayerItem(Gui gui, Player player, ReportGroup group, ReportStatus status, int slot) {

        List<Report> reports = group.getReportsByStatus(status);

        int total = group.getReports().size();

        int open = group.count(ReportStatus.OPEN);

        int resolved = group.count(ReportStatus.RESOLVED);

        int dismissed = group.count(ReportStatus.DISMISSED);

        String server = getLatestServer(group, status);

        GuiItem item = GuiItem.item(Material.PLAYER_HEAD).name("<red>" + escape(group.getTargetName())).lore("<gray>Reports: <white>" + total,

                "<gray>Abertos: <red>" + open,

                "<gray>Resolvidos: <green>" + resolved,

                "<gray>Descartados: <dark_gray>" + dismissed,

                "",

                "<gray>Reports neste filtro: <white>" + reports.size(),

                "<gray>Servidor: <yellow>" + escape(server),

                "",

                "<yellow>Clique esquerdo" + " <gray>para ver o histórico.",

                "<green>Clique direito" + " <gray>para entrar no servidor.",

                "<red>Shift + clique" + " <gray>para sugerir um ban.");

        /*
         * A cabeça é aplicada separadamente porque
         * GuiItem.playerHead() utiliza um Player online.
         *
         * Caso o jogador esteja offline,
         * permanece uma PLAYER_HEAD normal.
         */

        Player target = Bukkit.getPlayer(group.getTargetUniqueId());

        if (target != null) {
            item.playerHead(target);
        }

        item.onClick(event -> {

            /*
             * =========================
             * SHIFT + CLIQUE
             * =========================
             */

            if (event.getClickType().isShiftClick()) {

                suggestBan(event.getPlayer(), group, status);

                return;
            }

            /*
             * =========================
             * BOTÃO DIREITO
             * =========================
             */

            if (event.getClickType().isRightClick()) {

                connectToServer(event.getPlayer(), group, status);

                return;
            }

            /*
             * =========================
             * BOTÃO ESQUERDO
             * =========================
             */

            openHistory(event.getPlayer(), group, status);
        });

        gui.setItem(slot, item);
    }

    private void buildNavigation(Gui gui, Player player, ReportStatus status, int page, int totalPages) {

        if (page > 0) {

            gui.setItem(PREVIOUS_PAGE_SLOT, GuiItem.item(Material.ARROW).name("<yellow>Página anterior").lore("<gray>Voltar para a página", "<gray>anterior.").onClick(event -> open(player, status, page - 1)));

        } else {

            gui.setItem(PREVIOUS_PAGE_SLOT, GuiItem.item(Material.GRAY_STAINED_GLASS_PANE).name("<dark_gray> "));
        }

        if (totalPages > 0 && page + 1 < totalPages) {

            gui.setItem(NEXT_PAGE_SLOT, GuiItem.item(Material.ARROW).name("<yellow>Próxima página").lore("<gray>Avançar para a próxima", "<gray>página.").onClick(event -> open(player, status, page + 1)));

        } else {

            gui.setItem(NEXT_PAGE_SLOT, GuiItem.item(Material.GRAY_STAINED_GLASS_PANE).name("<dark_gray> "));
        }
    }

    private void buildFilter(Gui gui, Player player, ReportStatus currentStatus, int page) {

        gui.setItem(CURRENT_FILTER_SLOT, GuiItem.item(getFilterMaterial(currentStatus)).name("<white>Filtro atual: <yellow>" + getStatusName(currentStatus)).lore("<gray>Exibindo somente reports", "<gray>com o status selecionado."));

        gui.setItem(FILTER_SLOT, GuiItem.item(Material.HOPPER).name("<yellow>Filtrar reports").lore(getFilterLine(ReportStatus.OPEN, currentStatus), getFilterLine(ReportStatus.RESOLVED, currentStatus), getFilterLine(ReportStatus.DISMISSED, currentStatus), "", "<gray>Clique para alternar o filtro.").onClick(event -> {

            ReportStatus next = getNextStatus(currentStatus);

            open(player, next, 0);
        }));
    }

    private void buildCloseButton(Gui gui) {

        gui.setItem(CLOSE_SLOT, GuiItem.item(Material.BARRIER).name("<red>Fechar").lore("<gray>Clique para fechar.").onClick(GuiClickEvent::close));
    }

    /*
     * =========================
     * HISTÓRICO
     * =========================
     */

    private void openHistory(Player player, ReportGroup group, ReportStatus status) {

        ReportHistoryGui historyGui = new ReportHistoryGui(guiManager, reportManager, anvilGuiManager);

        historyGui.open(player, group.getTargetUniqueId(), group.getTargetName(), status);
    }

    /*
     * =========================
     * BAN
     * =========================
     */

    private void suggestBan(Player player, ReportGroup group, ReportStatus status) {

        List<Report> reports = group.getReportsByStatus(status);

        if (reports.isEmpty()) {

            player.sendMessage("§cNão existem reports neste filtro.");

            return;
        }

        Report latest = reports.get(0);

        player.closeInventory();

        player.sendMessage("§c§lBAN §8» §7Sugestão de comando:");

        player.sendMessage("§f/ban " + group.getTargetName() + " " + latest.getReason() + " <duração>");
    }

    /*
     * =========================
     * SERVIDOR
     * =========================
     */

    private void connectToServer(Player player, ReportGroup group, ReportStatus status) {

        List<Report> reports = group.getReportsByStatus(status);

        if (reports.isEmpty()) {

            player.sendMessage("§cNão existem reports neste filtro.");

            return;
        }

        Report latest = reports.get(0);

        player.sendMessage("§e§lREPORT §8» §7Servidor registrado: §f" + latest.getServer());

        player.sendMessage("§7A conexão pelo proxy será adicionada " + "na próxima etapa.");
    }

    private String getLatestServer(ReportGroup group, ReportStatus status) {

        List<Report> reports = group.getReportsByStatus(status);

        if (reports.isEmpty()) {
            return "-";
        }

        return reports.get(0).getServer();
    }

    private String getFilterLine(ReportStatus status, ReportStatus currentStatus) {

        if (status == currentStatus) {

            return "<green>➜ " + getStatusName(status);
        }

        return "<gray>   " + getStatusName(status);
    }

    private Material getFilterMaterial(ReportStatus status) {

        return switch (status) {

            case OPEN -> Material.LIME_DYE;

            case RESOLVED -> Material.GREEN_DYE;

            case DISMISSED -> Material.GRAY_DYE;
        };
    }

    private ReportStatus getNextStatus(ReportStatus status) {

        return switch (status) {

            case OPEN -> ReportStatus.RESOLVED;

            case RESOLVED -> ReportStatus.DISMISSED;

            case DISMISSED -> ReportStatus.OPEN;
        };
    }

    private String getStatusName(ReportStatus status) {

        return switch (status) {

            case OPEN -> "Abertos";

            case RESOLVED -> "Resolvidos";

            case DISMISSED -> "Descartados";
        };
    }

    private int calculateTotalPages(int size) {

        if (size <= 0) {
            return 0;
        }

        return (size + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("<", "\\<").replace(">", "\\>");
    }

    /*
     * =========================
     * REPORT GROUP
     * =========================
     */

    private static final class ReportGroup {

        private final UUID targetUniqueId;
        private final String targetName;

        private final List<Report> reports = new ArrayList<>();

        private ReportGroup(UUID targetUniqueId, String targetName) {

            this.targetUniqueId = targetUniqueId;

            this.targetName = targetName;
        }

        private void add(Report report) {

            reports.add(report);

            reports.sort(Comparator.comparing(Report::getCreatedAt).reversed());
        }

        private UUID getTargetUniqueId() {
            return targetUniqueId;
        }

        private String getTargetName() {
            return targetName;
        }

        private List<Report> getReports() {

            return List.copyOf(reports);
        }

        private List<Report> getReportsByStatus(ReportStatus status) {

            return reports.stream().filter(report -> report.getStatus() == status).toList();
        }

        private int count(ReportStatus status) {

            return (int) reports.stream().filter(report -> report.getStatus() == status).count();
        }

        private java.time.Instant getLatestReportDate() {

            if (reports.isEmpty()) {
                return java.time.Instant.MIN;
            }

            return reports.get(0).getCreatedAt();
        }
    }
}