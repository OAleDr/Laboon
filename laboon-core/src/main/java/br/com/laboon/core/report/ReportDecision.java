package br.com.laboon.core.report;

import java.time.Instant;
import java.util.UUID;

public final class ReportDecision {

    private final String id;
    private final String reportId;

    private final ReportStatus previousStatus;
    private final ReportStatus newStatus;

    private final UUID staffUniqueId;
    private final String staffName;

    private final String reason;
    private final Instant createdAt;

    public ReportDecision(String id, String reportId, ReportStatus previousStatus, ReportStatus newStatus, UUID staffUniqueId, String staffName, String reason, Instant createdAt) {

        this.id = id;
        this.reportId = reportId;

        this.previousStatus = previousStatus;

        this.newStatus = newStatus;

        this.staffUniqueId = staffUniqueId;

        this.staffName = staffName;

        this.reason = reason;

        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getReportId() {
        return reportId;
    }

    public ReportStatus getPreviousStatus() {
        return previousStatus;
    }

    public ReportStatus getNewStatus() {
        return newStatus;
    }

    public UUID getStaffUniqueId() {
        return staffUniqueId;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}