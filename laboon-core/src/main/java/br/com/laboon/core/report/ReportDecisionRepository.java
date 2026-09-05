package br.com.laboon.core.report;

import br.com.laboon.core.redis.RedisManager;

import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ReportDecisionRepository {

    private static final String DECISION_PREFIX = "laboon:report:decision:";

    private static final String DECISIONS_PREFIX = "laboon:report:decisions:";

    private final RedisManager redisManager;

    public ReportDecisionRepository(RedisManager redisManager) {

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

    public void save(ReportDecision decision) {

        if (decision == null) {
            return;
        }

        Jedis jedis = redisManager.getJedis();

        String key = DECISION_PREFIX + decision.getId();

        Map<String, String> fields = new HashMap<>();

        fields.put("reportId", decision.getReportId());

        fields.put("previousStatus", decision.getPreviousStatus().name());

        fields.put("newStatus", decision.getNewStatus().name());

        fields.put("staffUniqueId", decision.getStaffUniqueId().toString());

        fields.put("staffName", decision.getStaffName());

        fields.put("reason", decision.getReason());

        fields.put("createdAt", decision.getCreatedAt().toString());

        jedis.hset(key, fields);

        jedis.sadd(DECISIONS_PREFIX + decision.getReportId(), decision.getId());
    }

    /*
     * ==================================================
     * FIND BY ID
     * ==================================================
     */

    public ReportDecision findById(String id) {

        if (id == null || id.isBlank()) {
            return null;
        }

        Jedis jedis = redisManager.getJedis();

        Map<String, String> fields = jedis.hgetAll(DECISION_PREFIX + id);

        if (fields == null || fields.isEmpty()) {
            return null;
        }

        return deserialize(id, fields);
    }

    /*
     * ==================================================
     * FIND BY REPORT
     * ==================================================
     */

    public List<ReportDecision> findByReport(String reportId) {

        if (reportId == null || reportId.isBlank()) {
            return List.of();
        }

        Jedis jedis = redisManager.getJedis();

        Set<String> ids = jedis.smembers(DECISIONS_PREFIX + reportId);

        List<ReportDecision> decisions = new ArrayList<>();

        for (String id : ids) {

            ReportDecision decision = findById(id);

            if (decision != null) {
                decisions.add(decision);
            }
        }

        decisions.sort(Comparator.comparing(ReportDecision::getCreatedAt));

        return List.copyOf(decisions);
    }

    /*
     * ==================================================
     * DELETE DECISION
     * ==================================================
     */

    public void delete(String id) {

        if (id == null || id.isBlank()) {
            return;
        }

        ReportDecision decision = findById(id);

        Jedis jedis = redisManager.getJedis();

        jedis.del(DECISION_PREFIX + id);

        if (decision != null) {

            jedis.srem(DECISIONS_PREFIX + decision.getReportId(), id);
        }
    }

    /*
     * ==================================================
     * DELETE ALL DECISIONS
     * ==================================================
     */

    public void deleteByReport(String reportId) {

        if (reportId == null || reportId.isBlank()) {
            return;
        }

        Jedis jedis = redisManager.getJedis();

        String indexKey = DECISIONS_PREFIX + reportId;

        Set<String> ids = jedis.smembers(indexKey);

        if (ids != null) {

            for (String id : ids) {

                jedis.del(DECISION_PREFIX + id);
            }
        }

        /*
         * Apaga o índice do histórico.
         */

        jedis.del(indexKey);
    }

    /*
     * ==================================================
     * DESERIALIZE
     * ==================================================
     */

    private ReportDecision deserialize(String id, Map<String, String> fields) {

        try {

            return new ReportDecision(

                    id,

                    fields.get("reportId"),

                    ReportStatus.valueOf(fields.get("previousStatus")),

                    ReportStatus.valueOf(fields.get("newStatus")),

                    UUID.fromString(fields.get("staffUniqueId")),

                    fields.get("staffName"),

                    fields.get("reason"),

                    Instant.parse(fields.get("createdAt")));

        } catch (Exception exception) {

            return null;
        }
    }
}