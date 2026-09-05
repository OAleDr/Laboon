package br.com.laboon.core.report;

import br.com.laboon.core.redis.RedisManager;
import redis.clients.jedis.JedisPooled;

import java.time.Instant;
import java.util.*;

public final class ReportRepository {

    private static final String REPORT_PREFIX = "laboon:report:";

    private static final String REPORTS_KEY = "laboon:reports";

    private final RedisManager redisManager;

    public ReportRepository(RedisManager redisManager) {

        if (redisManager == null) {
            throw new IllegalArgumentException("RedisManager não pode ser nulo.");
        }

        this.redisManager = redisManager;
    }

    /*
     * ==================================================
     * SAVE
     * ==================================================
     */

    public void save(Report report) {

        if (report == null) {
            return;
        }

        JedisPooled jedis = redisManager.getJedis();

        String key = REPORT_PREFIX + report.getId();

        Map<String, String> fields = new HashMap<>();

        fields.put("reporterUniqueId", report.getReporterUniqueId().toString());

        fields.put("reporterName", report.getReporterName());

        fields.put("targetUniqueId", report.getTargetUniqueId().toString());

        fields.put("targetName", report.getTargetName());

        fields.put("reason", report.getReason());

        fields.put("server", report.getServer());

        fields.put("createdAt", report.getCreatedAt().toString());

        fields.put("status", report.getStatus().name());

        /*
         * Compatibilidade:
         *
         * Se algum report antigo não tiver
         * statusChangedAt, o carregamento
         * utilizará createdAt.
         */
        if (report.getStatusChangedAt() != null) {

            fields.put("statusChangedAt", report.getStatusChangedAt().toString());
        }

        jedis.hset(key, fields);

        jedis.sadd(REPORTS_KEY, report.getId());
    }

    /*
     * ==================================================
     * FIND BY ID
     * ==================================================
     */

    public Report findById(String id) {

        if (id == null || id.isBlank()) {
            return null;
        }

        JedisPooled jedis = redisManager.getJedis();

        Map<String, String> fields = jedis.hgetAll(REPORT_PREFIX + id);

        if (fields == null || fields.isEmpty()) {
            return null;
        }

        return deserialize(id, fields);
    }

    /*
     * ==================================================
     * FIND ALL
     * ==================================================
     */

    public List<Report> findAll() {

        JedisPooled jedis = redisManager.getJedis();

        var ids = jedis.smembers(REPORTS_KEY);

        List<Report> reports = new ArrayList<>();

        for (String id : ids) {

            Report report = findById(id);

            if (report != null) {
                reports.add(report);
            }
        }

        reports.sort(Comparator.comparing(Report::getCreatedAt).reversed());

        return List.copyOf(reports);
    }

    /*
     * ==================================================
     * FIND BY STATUS
     * ==================================================
     */

    public List<Report> findByStatus(ReportStatus status) {

        if (status == null) {
            return List.of();
        }

        return findAll().stream().filter(report -> report.getStatus() == status).toList();
    }

    /*
     * ==================================================
     * FIND BY TARGET
     * ==================================================
     */

    public List<Report> findByTarget(UUID targetUniqueId) {

        if (targetUniqueId == null) {
            return List.of();
        }

        return findAll().stream().filter(report -> targetUniqueId.equals(report.getTargetUniqueId())).toList();
    }

    /*
     * ==================================================
     * FIND BY TARGET + STATUS
     * ==================================================
     */

    public List<Report> findByTargetAndStatus(UUID targetUniqueId, ReportStatus status) {

        if (targetUniqueId == null || status == null) {
            return List.of();
        }

        return findAll().stream().filter(report -> targetUniqueId.equals(report.getTargetUniqueId())).filter(report -> report.getStatus() == status).toList();
    }

    /*
     * ==================================================
     * EXISTS
     * ==================================================
     */

    public boolean exists(String id) {

        if (id == null || id.isBlank()) {
            return false;
        }

        JedisPooled jedis = redisManager.getJedis();

        return jedis.exists(REPORT_PREFIX + id);
    }

    /*
     * ==================================================
     * UPDATE STATUS
     * ==================================================
     */

    public void updateStatus(String id, ReportStatus status) {

        if (id == null || id.isBlank() || status == null) {
            return;
        }

        Report report = findById(id);

        if (report == null) {
            return;
        }

        report.setStatus(status);

        /*
         * Toda alteração de status
         * começa uma nova contagem.
         */
        report.setStatusChangedAt(Instant.now());

        save(report);
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

        JedisPooled jedis = redisManager.getJedis();

        jedis.del(REPORT_PREFIX + id);

        jedis.srem(REPORTS_KEY, id);
    }

    /*
     * ==================================================
     * DESERIALIZE
     * ==================================================
     */

    private Report deserialize(String id, Map<String, String> fields) {

        try {

            UUID reporterUniqueId = UUID.fromString(fields.get("reporterUniqueId"));

            UUID targetUniqueId = UUID.fromString(fields.get("targetUniqueId"));

            Instant createdAt = Instant.parse(fields.get("createdAt"));

            Report report = new Report(id,

                    reporterUniqueId, fields.get("reporterName"),

                    targetUniqueId, fields.get("targetName"),

                    fields.get("reason"),

                    fields.get("server"),

                    createdAt);

            String status = fields.get("status");

            if (status != null && !status.isBlank()) {

                try {

                    report.setStatus(ReportStatus.valueOf(status));

                } catch (IllegalArgumentException ignored) {
                }
            }

            /*
             * Reports novos terão esse campo.
             *
             * Reports antigos podem não ter.
             * Nesse caso usamos createdAt.
             */

            String statusChangedAt = fields.get("statusChangedAt");

            if (statusChangedAt != null && !statusChangedAt.isBlank()) {

                report.setStatusChangedAt(Instant.parse(statusChangedAt));

            } else {

                report.setStatusChangedAt(createdAt);
            }

            return report;

        } catch (Exception exception) {

            return null;
        }
    }
}