package br.com.laboon.core.account.repository;

import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class PostgreSqlTemporaryGroupRepository {

    private final DatabaseManager database;

    public PostgreSqlTemporaryGroupRepository(
            DatabaseManager database
    ) {

        if (database == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager não pode ser nulo."
            );
        }

        this.database = database;
    }

    public Map<Group, Instant> findAll(UUID uniqueId) {

        Map<Group, Instant> groups =
                new EnumMap<>(Group.class);

        if (uniqueId == null) {
            return groups;
        }

        final String sql = """
                SELECT
                    "group",
                    "expiresAt"
                FROM "temporary_groups"
                WHERE "uniqueId" = ?
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    uniqueId.toString()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    String groupName =
                            resultSet.getString(
                                    "group"
                            );

                    Timestamp timestamp =
                            resultSet.getTimestamp(
                                    "expiresAt"
                            );

                    if (groupName == null
                            || timestamp == null) {
                        continue;
                    }

                    try {

                        Group group =
                                Group.valueOf(groupName);

                        Instant expiresAt =
                                timestamp.toInstant();

                        if (expiresAt.isAfter(
                                Instant.now()
                        )) {

                            groups.put(
                                    group,
                                    expiresAt
                            );
                        }

                    } catch (
                            IllegalArgumentException ignored
                    ) {
                    }
                }
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao carregar grupos temporários: "
                            + uniqueId,
                    exception
            );
        }

        return groups;
    }

    public void save(
            UUID uniqueId,
            Group group,
            Instant expiresAt
    ) {

        if (uniqueId == null
                || group == null
                || expiresAt == null) {

            return;
        }

        final String sql = """
                INSERT INTO "temporary_groups" (
                    "uniqueId",
                    "group",
                    "expiresAt"
                )
                VALUES (?, ?::"Group", ?)
                ON CONFLICT ("uniqueId", "group")
                DO UPDATE SET
                    "expiresAt" =
                        EXCLUDED."expiresAt"
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    uniqueId.toString()
            );

            statement.setString(
                    2,
                    group.name()
            );

            statement.setTimestamp(
                    3,
                    Timestamp.from(expiresAt)
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao salvar grupo temporário: "
                            + uniqueId,
                    exception
            );
        }
    }

    public void delete(
            UUID uniqueId,
            Group group
    ) {

        if (uniqueId == null || group == null) {
            return;
        }

        final String sql = """
                DELETE FROM "temporary_groups"
                WHERE "uniqueId" = ?
                  AND "group" = ?::"Group"
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    uniqueId.toString()
            );

            statement.setString(
                    2,
                    group.name()
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao remover grupo temporário: "
                            + uniqueId,
                    exception
            );
        }
    }

    public void deleteExpired() {

        final String sql = """
                DELETE FROM "temporary_groups"
                WHERE "expiresAt" <= CURRENT_TIMESTAMP
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao limpar grupos temporários expirados.",
                    exception
            );
        }
    }

    public void deleteAll(UUID uniqueId) {

        if (uniqueId == null) {
            return;
        }

        final String sql = """
                DELETE FROM "temporary_groups"
                WHERE "uniqueId" = ?
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    uniqueId.toString()
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao deletar grupos temporários: "
                            + uniqueId,
                    exception
            );
        }
    }
}