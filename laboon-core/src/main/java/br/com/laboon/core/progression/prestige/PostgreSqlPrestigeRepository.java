package br.com.laboon.core.progression.prestige;

import br.com.laboon.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public final class PostgreSqlPrestigeRepository
        implements PrestigeRepository, PrestigeTransactionRepository {

    private final DatabaseManager database;

    public PostgreSqlPrestigeRepository(
            DatabaseManager database
    ) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager não pode ser nulo."
            );
        }

        this.database = database;
    }

    @Override
    public int getPrestige(UUID playerUuid) {
        validatePlayer(playerUuid);

        String sql = """
                SELECT prestige
                FROM laboon_progression_prestige
                WHERE player_uuid = ?
                """;

        try (
                Connection connection =
                        database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setObject(
                    1,
                    playerUuid
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                if (!result.next()) {
                    return 0;
                }

                return result.getInt(
                        "prestige"
                );
            }

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Não foi possível carregar o prestige.",
                    exception
            );
        }
    }

    @Override
    public void setPrestige(
            UUID playerUuid,
            int prestige
    ) {
        validatePlayer(playerUuid);
        validatePrestige(prestige);

        String sql = """
                INSERT INTO laboon_progression_prestige (
                    player_uuid,
                    prestige,
                    updated_at
                )
                VALUES (?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (player_uuid)
                DO UPDATE SET
                    prestige = EXCLUDED.prestige,
                    updated_at = CURRENT_TIMESTAMP
                """;

        try (
                Connection connection =
                        database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setObject(
                    1,
                    playerUuid
            );

            statement.setInt(
                    2,
                    prestige
            );

            statement.executeUpdate();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Não foi possível salvar o prestige.",
                    exception
            );
        }
    }

    @Override
    public int getPrestige(
            Connection connection,
            UUID playerUuid
    ) {
        validatePlayer(playerUuid);

        String sql = """
                SELECT prestige
                FROM laboon_progression_prestige
                WHERE player_uuid = ?
                FOR UPDATE
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setObject(
                    1,
                    playerUuid
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                if (!result.next()) {
                    return 0;
                }

                return result.getInt(
                        "prestige"
                );
            }

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Não foi possível carregar o prestige na transação.",
                    exception
            );
        }
    }

    @Override
    public void setPrestige(
            Connection connection,
            UUID playerUuid,
            int prestige
    ) {
        validatePlayer(playerUuid);
        validatePrestige(prestige);

        String sql = """
                INSERT INTO laboon_progression_prestige (
                    player_uuid,
                    prestige,
                    updated_at
                )
                VALUES (?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (player_uuid)
                DO UPDATE SET
                    prestige = EXCLUDED.prestige,
                    updated_at = CURRENT_TIMESTAMP
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setObject(
                    1,
                    playerUuid
            );

            statement.setInt(
                    2,
                    prestige
            );

            statement.executeUpdate();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Não foi possível salvar o prestige na transação.",
                    exception
            );
        }
    }

    private void validatePlayer(UUID playerUuid) {
        if (playerUuid == null) {
            throw new IllegalArgumentException(
                    "UUID do player não pode ser nulo."
            );
        }
    }

    private void validatePrestige(int prestige) {
        if (prestige < 0) {
            throw new IllegalArgumentException(
                    "Prestige não pode ser negativo."
            );
        }
    }
}