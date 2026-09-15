package br.com.laboon.core.rewards;

import br.com.laboon.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class PostgreSqlRewardClaimRepository
        implements RewardClaimRepository {

    private final DatabaseManager databaseManager;

    public PostgreSqlRewardClaimRepository(
            DatabaseManager databaseManager
    ) {

        if (databaseManager == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager não pode ser nulo."
            );
        }

        this.databaseManager = databaseManager;
    }

    @Override
    public boolean tryClaim(
            String rewardId,
            UUID playerUuid,
            RewardSource source
    ) {

        validateRewardId(rewardId);
        validatePlayerUuid(playerUuid);

        RewardSource safeSource =
                source == null
                        ? RewardSource.OTHER
                        : source;

        String sql = """
                INSERT INTO laboon_reward_claims
                    (
                        reward_id,
                        player_uuid,
                        source
                    )
                VALUES
                    (?, ?, ?)
                ON CONFLICT (reward_id)
                DO NOTHING
                """;

        try (
                Connection connection =
                        databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    rewardId
            );

            statement.setObject(
                    2,
                    playerUuid
            );

            statement.setString(
                    3,
                    safeSource.name()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao registrar claim da recompensa: "
                            + rewardId,
                    exception
            );
        }
    }

    @Override
    public boolean hasClaimed(
            String rewardId
    ) {

        validateRewardId(rewardId);

        String sql = """
                SELECT 1
                FROM laboon_reward_claims
                WHERE reward_id = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    rewardId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao verificar claim da recompensa: "
                            + rewardId,
                    exception
            );
        }
    }

    private void validateRewardId(
            String rewardId
    ) {

        if (
                rewardId == null
                        || rewardId.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Reward ID não pode ser nulo ou vazio."
            );
        }

        if (rewardId.length() > 128) {

            throw new IllegalArgumentException(
                    "Reward ID não pode possuir mais de 128 caracteres."
            );
        }
    }

    private void validatePlayerUuid(
            UUID playerUuid
    ) {

        if (playerUuid == null) {

            throw new IllegalArgumentException(
                    "UUID do jogador não pode ser nulo."
            );
        }
    }
}