package br.com.laboon.core.report;

import br.com.laboon.core.redis.RedisManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ReportManager {

    private final ReportRepository repository;
    private final ReportDecisionRepository decisionRepository;

    public ReportManager(RedisManager redisManager) {

        this.repository = new ReportRepository(redisManager);

        this.decisionRepository = new ReportDecisionRepository(redisManager);
    }

    /*
     * ==================================================
     * CREATE
     * ==================================================
     */

    public Report create(UUID reporterUniqueId, String reporterName, UUID targetUniqueId, String targetName, String reason, String server) {

        Report report = new Report(

                UUID.randomUUID().toString(),

                reporterUniqueId, reporterName,

                targetUniqueId, targetName,

                reason, server,

                Instant.now());

        repository.save(report);

        return report;
    }

    /*
     * ==================================================
     * FIND
     * ==================================================
     */

    public Report find(String id) {

        return repository.findById(id);
    }

    public List<Report> findAll() {

        return repository.findAll();
    }

    public List<Report> findByStatus(ReportStatus status) {

        return repository.findByStatus(status);
    }

    public List<Report> findByTarget(UUID targetUniqueId) {

        return repository.findByTarget(targetUniqueId);
    }

    public List<Report> findByTargetAndStatus(UUID targetUniqueId, ReportStatus status) {

        return repository.findByTargetAndStatus(targetUniqueId, status);
    }

    public boolean exists(String id) {

        return repository.exists(id);
    }

    /*
     * ==================================================
     * RESOLVE
     * ==================================================
     */

    public void resolve(String id, UUID staffUniqueId, String staffName, String reason) {

        changeStatus(id, ReportStatus.RESOLVED, staffUniqueId, staffName, reason);
    }

    /*
     * ==================================================
     * DISMISS
     * ==================================================
     */

    public void dismiss(String id, UUID staffUniqueId, String staffName, String reason) {

        changeStatus(id, ReportStatus.DISMISSED, staffUniqueId, staffName, reason);
    }

    /*
     * ==================================================
     * REOPEN
     * ==================================================
     */

    public void reopen(String id, UUID staffUniqueId, String staffName, String reason) {

        changeStatus(id, ReportStatus.OPEN, staffUniqueId, staffName, reason);
    }

    /*
     * ==================================================
     * CHANGE STATUS
     * ==================================================
     */

    private void changeStatus(String id, ReportStatus newStatus, UUID staffUniqueId, String staffName, String reason) {

        if (id == null || id.isBlank()) {
            return;
        }

        if (newStatus == null) {
            return;
        }

        if (staffUniqueId == null) {
            return;
        }

        if (staffName == null || staffName.isBlank()) {
            return;
        }

        if (reason == null || reason.isBlank()) {
            return;
        }

        Report report = repository.findById(id);

        if (report == null) {
            return;
        }

        ReportStatus previousStatus = report.getStatus();

        /*
         * Não cria decisão se o status
         * já for o mesmo.
         */

        if (previousStatus == newStatus) {
            return;
        }

        Instant now = Instant.now();

        /*
         * ==================================================
         * HISTÓRICO
         * ==================================================
         */

        ReportDecision decision = new ReportDecision(

                UUID.randomUUID().toString(),

                id,

                previousStatus, newStatus,

                staffUniqueId, staffName,

                reason,

                now);

        decisionRepository.save(decision);

        /*
         * ==================================================
         * REPORT
         * ==================================================
         */

        report.setStatus(newStatus);

        /*
         * Toda mudança de status
         * reinicia a contagem das 24h.
         */

        report.setStatusChangedAt(now);

        repository.save(report);
    }

    /*
     * ==================================================
     * HISTORY
     * ==================================================
     */

    public List<ReportDecision> findDecisions(String reportId) {

        return decisionRepository.findByReport(reportId);
    }

    /*
     * ==================================================
     * DELETE
     * ==================================================
     */

    public void delete(String id) {

        if (id == null || id.isBlank()) {
            return;
        }

        /*
         * Primeiro remove todas as decisões.
         */

        decisionRepository.deleteByReport(id);

        /*
         * Depois remove o report.
         */

        repository.delete(id);
    }

    /*
     * ==================================================
     * MÉTODOS ANTIGOS
     * ==================================================
     *
     * Mantidos para compatibilidade.
     *
     * Não utilizar nas GUIs novas.
     */

    public void resolve(String id) {

        repository.updateStatus(id, ReportStatus.RESOLVED);
    }

    public void dismiss(String id) {

        repository.updateStatus(id, ReportStatus.DISMISSED);
    }

    public void reopen(String id) {

        repository.updateStatus(id, ReportStatus.OPEN);
    }
}