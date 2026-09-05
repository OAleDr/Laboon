package br.com.laboon.core.report;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class ReportExpirationService {

    private static final Duration EXPIRATION_TIME = Duration.ofHours(24);

    private final ReportManager reportManager;

    public ReportExpirationService(ReportManager reportManager) {

        if (reportManager == null) {
            throw new IllegalArgumentException("ReportManager não pode ser nulo.");
        }

        this.reportManager = reportManager;
    }

    /*
     * ==================================================
     * CHECK
     * ==================================================
     */

    public void checkExpiredReports() {

        /*
         * IMPORTANTE:
         *
         * Procuramos somente RESOLVED
         * e DISMISSED.
         *
         * OPEN jamais entra aqui.
         */

        checkStatus(ReportStatus.RESOLVED);

        checkStatus(ReportStatus.DISMISSED);
    }

    /*
     * ==================================================
     * CHECK STATUS
     * ==================================================
     */

    private void checkStatus(ReportStatus status) {

        List<Report> reports = reportManager.findByStatus(status);

        Instant now = Instant.now();

        for (Report report : reports) {

            if (report == null) {
                continue;
            }

            /*
             * Proteção adicional:
             *
             * Mesmo que alguém altere a lógica
             * no futuro, OPEN nunca será apagado.
             */

            if (report.getStatus() == ReportStatus.OPEN) {
                continue;
            }

            Instant statusChangedAt = report.getStatusChangedAt();

            if (statusChangedAt == null) {
                continue;
            }

            Duration elapsed = Duration.between(statusChangedAt, now);

            if (elapsed.compareTo(EXPIRATION_TIME) >= 0) {

                reportManager.delete(report.getId());
            }
        }
    }
}