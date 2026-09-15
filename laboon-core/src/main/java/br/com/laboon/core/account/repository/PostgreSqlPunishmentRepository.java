package br.com.laboon.core.account.repository;

import br.com.laboon.core.account.punishment.Ban;
import br.com.laboon.core.account.punishment.Kick;
import br.com.laboon.core.account.punishment.Mute;
import br.com.laboon.core.account.punishment.PunishmentHistory;
import br.com.laboon.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;

public final class PostgreSqlPunishmentRepository {

    private final DatabaseManager database;

    public PostgreSqlPunishmentRepository(
            DatabaseManager database
    ) {

        if (database == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager não pode ser nulo."
            );
        }

        this.database = database;
    }

    /*
     * ============================================================
     * LOAD
     * ============================================================
     */

    public PunishmentHistory findHistory(
            UUID uniqueId
    ) {

        PunishmentHistory history =
                new PunishmentHistory();

        if (uniqueId == null) {
            return history;
        }

        loadBans(uniqueId, history);
        loadMutes(uniqueId, history);
        loadKicks(uniqueId, history);

        return history;
    }

    private void loadBans(
            UUID uniqueId,
            PunishmentHistory history
    ) {

        final String sql = """
                SELECT
                    "punishedBy",
                    "punishedByUuid",
                    "ip",
                    "server",
                    "punishmentTime",
                    "reason",
                    "expiresAt",
                    "removed",
                    "removedBy",
                    "removedByUuid",
                    "removedAt"
                FROM "punishments"
                WHERE "uniqueId" = ?
                  AND "type" = 'BAN'
                ORDER BY "punishmentTime" ASC
                """;

        try (
                Connection connection =
                        database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    uniqueId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    Ban ban =
                            mapBan(resultSet);

                    if (ban != null) {
                        history.addBan(ban);
                    }
                }
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao carregar bans: "
                            + uniqueId,
                    exception
            );
        }
    }

    private void loadMutes(
            UUID uniqueId,
            PunishmentHistory history
    ) {

        final String sql = """
                SELECT
                    "punishedBy",
                    "punishedByUuid",
                    "ip",
                    "server",
                    "punishmentTime",
                    "reason",
                    "expiresAt",
                    "removed",
                    "removedBy",
                    "removedByUuid",
                    "removedAt"
                FROM "punishments"
                WHERE "uniqueId" = ?
                  AND "type" = 'MUTE'
                ORDER BY "punishmentTime" ASC
                """;

        try (
                Connection connection =
                        database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    uniqueId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    Mute mute =
                            mapMute(resultSet);

                    if (mute != null) {
                        history.addMute(mute);
                    }
                }
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao carregar mutes: "
                            + uniqueId,
                    exception
            );
        }
    }

    private void loadKicks(
            UUID uniqueId,
            PunishmentHistory history
    ) {

        final String sql = """
                SELECT
                    "punishedBy",
                    "punishedByUuid",
                    "server",
                    "punishmentTime",
                    "reason"
                FROM "punishments"
                WHERE "uniqueId" = ?
                  AND "type" = 'KICK'
                ORDER BY "punishmentTime" ASC
                """;

        try (
                Connection connection =
                        database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    uniqueId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    String kickedBy =
                            resultSet.getString(
                                    "punishedBy"
                            );

                    UUID kickedByUuid =
                            getUuid(
                                    resultSet,
                                    "punishedByUuid"
                            );

                    String server =
                            resultSet.getString(
                                    "server"
                            );

                    Timestamp time =
                            resultSet.getTimestamp(
                                    "punishmentTime"
                            );

                    String reason =
                            resultSet.getString(
                                    "reason"
                            );

                    if (kickedBy == null
                            || kickedBy.isBlank()
                            || kickedByUuid == null
                            || time == null
                            || reason == null
                            || reason.isBlank()) {

                        continue;
                    }

                    try {

                        history.addKick(
                                new Kick(
                                        kickedBy,
                                        kickedByUuid,
                                        server,
                                        time.toInstant(),
                                        reason
                                )
                        );

                    } catch (
                            IllegalArgumentException ignored
                    ) {
                        // Registro inválido não interrompe
                        // o carregamento do restante do histórico.
                    }
                }
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao carregar kicks: "
                            + uniqueId,
                    exception
            );
        }
    }

    /*
     * ============================================================
     * MAPPING
     * ============================================================
     */

    private Ban mapBan(
            ResultSet resultSet
    ) throws SQLException {

        String bannedBy =
                resultSet.getString(
                        "punishedBy"
                );

        UUID bannedByUuid =
                getUuid(
                        resultSet,
                        "punishedByUuid"
                );

        Timestamp banTime =
                resultSet.getTimestamp(
                        "punishmentTime"
                );

        String reason =
                resultSet.getString(
                        "reason"
                );

        if (bannedBy == null
                || bannedBy.isBlank()
                || bannedByUuid == null
                || banTime == null
                || reason == null
                || reason.isBlank()) {

            return null;
        }

        String ip =
                resultSet.getString(
                        "ip"
                );

        String server =
                resultSet.getString(
                        "server"
                );

        Timestamp expire =
                resultSet.getTimestamp(
                        "expiresAt"
                );

        boolean removed =
                resultSet.getBoolean(
                        "removed"
                );

        String removedBy =
                resultSet.getString(
                        "removedBy"
                );

        UUID removedByUuid =
                getUuid(
                        resultSet,
                        "removedByUuid"
                );

        Timestamp removedAt =
                resultSet.getTimestamp(
                        "removedAt"
                );

        try {

            return Ban.restore(
                    bannedBy,
                    bannedByUuid,
                    ip,
                    server,
                    banTime.toInstant(),
                    reason,
                    expire != null
                            ? expire.toInstant()
                            : null,
                    removed,
                    removedBy,
                    removedByUuid,
                    removedAt != null
                            ? removedAt.toInstant()
                            : null
            );

        } catch (IllegalArgumentException exception) {

            return null;
        }
    }

    private Mute mapMute(
            ResultSet resultSet
    ) throws SQLException {

        String mutedBy =
                resultSet.getString(
                        "punishedBy"
                );

        UUID mutedByUuid =
                getUuid(
                        resultSet,
                        "punishedByUuid"
                );

        Timestamp muteTime =
                resultSet.getTimestamp(
                        "punishmentTime"
                );

        String reason =
                resultSet.getString(
                        "reason"
                );

        if (mutedBy == null
                || mutedBy.isBlank()
                || mutedByUuid == null
                || muteTime == null
                || reason == null
                || reason.isBlank()) {

            return null;
        }

        String ip =
                resultSet.getString(
                        "ip"
                );

        String server =
                resultSet.getString(
                        "server"
                );

        Timestamp expire =
                resultSet.getTimestamp(
                        "expiresAt"
                );

        boolean removed =
                resultSet.getBoolean(
                        "removed"
                );

        String removedBy =
                resultSet.getString(
                        "removedBy"
                );

        UUID removedByUuid =
                getUuid(
                        resultSet,
                        "removedByUuid"
                );

        Timestamp removedAt =
                resultSet.getTimestamp(
                        "removedAt"
                );

        try {

            return Mute.restore(
                    mutedBy,
                    mutedByUuid,
                    ip,
                    server,
                    muteTime.toInstant(),
                    reason,
                    expire != null
                            ? expire.toInstant()
                            : null,
                    removed,
                    removedBy,
                    removedByUuid,
                    removedAt != null
                            ? removedAt.toInstant()
                            : null
            );

        } catch (IllegalArgumentException exception) {

            return null;
        }
    }

    /*
     * ============================================================
     * INSERT
     * ============================================================
     */

    public void saveBan(
            UUID uniqueId,
            Ban ban
    ) {

        requireId(uniqueId);

        if (ban == null) {
            throw new IllegalArgumentException(
                    "Ban não pode ser nulo."
            );
        }

        final String sql = """
                INSERT INTO "punishments" (
                    "id",
                    "uniqueId",
                    "type",
                    "punishedBy",
                    "punishedByUuid",
                    "ip",
                    "server",
                    "punishmentTime",
                    "reason",
                    "expiresAt",
                    "removed",
                    "removedBy",
                    "removedByUuid",
                    "removedAt"
                )
                VALUES (
                    ?,
                    ?,
                    'BAN',
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
                """;

        savePunishment(
                uniqueId,
                UUID.randomUUID().toString(),
                ban.getBannedBy(),
                ban.getBannedByUniqueId(),
                ban.getBannedIp(),
                ban.getServer(),
                ban.getBanTime(),
                ban.getReason(),
                ban.getExpire(),
                ban.isUnbanned(),
                ban.getUnbannedBy(),
                ban.getUnbannedByUniqueId(),
                ban.getUnbanTime(),
                sql
        );
    }

    public void saveMute(
            UUID uniqueId,
            Mute mute
    ) {

        requireId(uniqueId);

        if (mute == null) {
            throw new IllegalArgumentException(
                    "Mute não pode ser nulo."
            );
        }

        final String sql = """
                INSERT INTO "punishments" (
                    "id",
                    "uniqueId",
                    "type",
                    "punishedBy",
                    "punishedByUuid",
                    "ip",
                    "server",
                    "punishmentTime",
                    "reason",
                    "expiresAt",
                    "removed",
                    "removedBy",
                    "removedByUuid",
                    "removedAt"
                )
                VALUES (
                    ?,
                    ?,
                    'MUTE',
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
                """;

        savePunishment(
                uniqueId,
                UUID.randomUUID().toString(),
                mute.getMutedBy(),
                mute.getMutedByUniqueId(),
                mute.getMutedIp(),
                mute.getServer(),
                mute.getMuteTime(),
                mute.getReason(),
                mute.getExpire(),
                mute.isUnmuted(),
                mute.getUnmutedBy(),
                mute.getUnmutedByUniqueId(),
                mute.getUnmuteTime(),
                sql
        );
    }

    public void saveKick(
            UUID uniqueId,
            Kick kick
    ) {

        requireId(uniqueId);

        if (kick == null) {
            throw new IllegalArgumentException(
                    "Kick não pode ser nulo."
            );
        }

        final String sql = """
                INSERT INTO "punishments" (
                    "id",
                    "uniqueId",
                    "type",
                    "punishedBy",
                    "punishedByUuid",
                    "server",
                    "punishmentTime",
                    "reason"
                )
                VALUES (
                    ?,
                    ?,
                    'KICK',
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
                """;

        try (
                Connection connection =
                        database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    UUID.randomUUID().toString()
            );

            statement.setObject(
                    2,
                    uniqueId
            );

            statement.setString(
                    3,
                    kick.getKickedBy()
            );

            statement.setString(
                    4,
                    kick.getKickedByUniqueId().toString()
            );

            setNullableString(
                    statement,
                    5,
                    kick.getServer()
            );

            statement.setTimestamp(
                    6,
                    Timestamp.from(
                            kick.getTime()
                    )
            );

            statement.setString(
                    7,
                    kick.getReason()
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao salvar kick.",
                    exception
            );
        }
    }

    /*
     * ============================================================
     * UPDATE BAN
     * ============================================================
     */

    public void updateBan(
            UUID uniqueId,
            Ban ban
    ) {

        requireId(uniqueId);

        if (ban == null) {
            throw new IllegalArgumentException(
                    "Ban não pode ser nulo."
            );
        }

        final String sql = """
                UPDATE "punishments"
                SET
                    "removed" = ?,
                    "removedBy" = ?,
                    "removedByUuid" = ?,
                    "removedAt" = ?
                WHERE "uniqueId" = ?
                  AND "type" = 'BAN'
                  AND "punishmentTime" = ?
                """;

        try (
                Connection connection =
                        database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setBoolean(
                    1,
                    ban.isUnbanned()
            );

            setNullableString(
                    statement,
                    2,
                    ban.getUnbannedBy()
            );

            setNullableUuid(
                    statement,
                    3,
                    ban.getUnbannedByUniqueId()
            );

            setNullableTimestamp(
                    statement,
                    4,
                    ban.getUnbanTime()
            );

            statement.setObject(
                    5,
                    uniqueId
            );

            statement.setTimestamp(
                    6,
                    Timestamp.from(
                            ban.getBanTime()
                    )
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao atualizar ban.",
                    exception
            );
        }
    }

    /*
     * ============================================================
     * UPDATE MUTE
     * ============================================================
     */

    public void updateMute(
            UUID uniqueId,
            Mute mute
    ) {

        requireId(uniqueId);

        if (mute == null) {
            throw new IllegalArgumentException(
                    "Mute não pode ser nulo."
            );
        }

        final String sql = """
                UPDATE "punishments"
                SET
                    "removed" = ?,
                    "removedBy" = ?,
                    "removedByUuid" = ?,
                    "removedAt" = ?
                WHERE "uniqueId" = ?
                  AND "type" = 'MUTE'
                  AND "punishmentTime" = ?
                """;

        try (
                Connection connection =
                        database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setBoolean(
                    1,
                    mute.isUnmuted()
            );

            setNullableString(
                    statement,
                    2,
                    mute.getUnmutedBy()
            );

            setNullableUuid(
                    statement,
                    3,
                    mute.getUnmutedByUniqueId()
            );

            setNullableTimestamp(
                    statement,
                    4,
                    mute.getUnmuteTime()
            );

            statement.setObject(
                    5,
                    uniqueId
            );

            statement.setTimestamp(
                    6,
                    Timestamp.from(
                            mute.getMuteTime()
                    )
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao atualizar mute.",
                    exception
            );
        }
    }

    /*
     * ============================================================
     * HELPERS
     * ============================================================
     */

    private void savePunishment(
            UUID uniqueId,
            String punishmentId,
            String punishedBy,
            UUID punishedByUuid,
            String ip,
            String server,
            Instant punishmentTime,
            String reason,
            Instant expiresAt,
            boolean removed,
            String removedBy,
            UUID removedByUuid,
            Instant removedAt,
            String sql
    ) {

        try (
                Connection connection =
                        database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    punishmentId
            );

            statement.setObject(
                    2,
                    uniqueId
            );

            statement.setString(
                    3,
                    punishedBy
            );

            setNullableUuid(
                    statement,
                    4,
                    punishedByUuid
            );

            setNullableString(
                    statement,
                    5,
                    ip
            );

            setNullableString(
                    statement,
                    6,
                    server
            );

            statement.setTimestamp(
                    7,
                    Timestamp.from(
                            punishmentTime
                    )
            );

            statement.setString(
                    8,
                    reason
            );

            setNullableTimestamp(
                    statement,
                    9,
                    expiresAt
            );

            statement.setBoolean(
                    10,
                    removed
            );

            setNullableString(
                    statement,
                    11,
                    removedBy
            );

            setNullableUuid(
                    statement,
                    12,
                    removedByUuid
            );

            setNullableTimestamp(
                    statement,
                    13,
                    removedAt
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao salvar punição.",
                    exception
            );
        }
    }

    private void requireId(
            UUID uniqueId
    ) {

        if (uniqueId == null) {
            throw new IllegalArgumentException(
                    "UUID da conta não pode ser nulo."
            );
        }
    }

    private UUID getUuid(
            ResultSet resultSet,
            String column
    ) throws SQLException {

        String value =
                resultSet.getString(column);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void setNullableUuid(
            PreparedStatement statement,
            int index,
            UUID value
    ) throws SQLException {

        if (value == null) {

            statement.setNull(
                    index,
                    Types.VARCHAR
            );

        } else {

            statement.setObject(
                    index,
                    value
            );
        }
    }

    private void setNullableString(
            PreparedStatement statement,
            int index,
            String value
    ) throws SQLException {

        if (value == null) {

            statement.setNull(
                    index,
                    Types.VARCHAR
            );

        } else {

            statement.setString(
                    index,
                    value
            );
        }
    }

    private void setNullableTimestamp(
            PreparedStatement statement,
            int index,
            Instant value
    ) throws SQLException {

        if (value == null) {

            statement.setNull(
                    index,
                    Types.TIMESTAMP
            );

        } else {

            statement.setTimestamp(
                    index,
                    Timestamp.from(value)
            );
        }
    }
}