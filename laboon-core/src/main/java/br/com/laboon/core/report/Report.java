package br.com.laboon.core.report;

import java.time.Instant;
import java.util.UUID;

public final class Report {

    private final String id;

    private final UUID reporterUniqueId;
    private final String reporterName;

    private final UUID targetUniqueId;
    private final String targetName;

    private final String reason;
    private final String server;

    private final Instant createdAt;

    private ReportStatus status;

    /*
     * Momento em que o status atual foi definido.
     *
     * OPEN:
     * representa a criação do report.
     *
     * RESOLVED/DISMISSED:
     * representa o momento da decisão.
     */
    private Instant statusChangedAt;

    public Report(String id, UUID reporterUniqueId, String reporterName, UUID targetUniqueId, String targetName, String reason, String server, Instant createdAt) {

        this.id = id;

        this.reporterUniqueId = reporterUniqueId;
        this.reporterName = reporterName;

        this.targetUniqueId = targetUniqueId;
        this.targetName = targetName;

        this.reason = reason;
        this.server = server;

        this.createdAt = createdAt;

        this.status = ReportStatus.OPEN;

        /*
         * Enquanto está aberto,
         * usamos a data de criação.
         */
        this.statusChangedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public UUID getReporterUniqueId() {
        return reporterUniqueId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public UUID getTargetUniqueId() {
        return targetUniqueId;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getReason() {
        return reason;
    }

    public String getServer() {
        return server;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {

        this.status = status == null ? ReportStatus.OPEN : status;
    }

    public Instant getStatusChangedAt() {
        return statusChangedAt;
    }

    public void setStatusChangedAt(Instant statusChangedAt) {

        this.statusChangedAt = statusChangedAt;
    }
}