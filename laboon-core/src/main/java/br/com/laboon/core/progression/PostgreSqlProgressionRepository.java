package br.com.laboon.core.progression;

import br.com.laboon.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class PostgreSqlProgressionRepository implements ProgressionRepository {

    private final DatabaseManager database;

    public PostgreSqlProgressionRepository(DatabaseManager database) {
        if (database == null) {
            throw new IllegalArgumentException("DatabaseManager não pode ser nulo.");
        }
        this.database = database;
    }

    @Override
    public long getExperience(UUID playerUuid, ProgressionGame game) {
        String sql = """
                SELECT experience
                FROM laboon_progression
                WHERE player_uuid = ? AND game = ?
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, playerUuid);
            statement.setString(2, game.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Erro ao buscar experiência de progressão.", exception);
        }
    }

    @Override
    public void setExperience(UUID playerUuid, ProgressionGame game, long experience) {
        String sql = """
                INSERT INTO laboon_progression (player_uuid, game, experience)
                VALUES (?, ?, ?)
                ON CONFLICT (player_uuid, game)
                DO UPDATE SET experience = EXCLUDED.experience,
                              updated_at = CURRENT_TIMESTAMP
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, playerUuid);
            statement.setString(2, game.name());
            statement.setLong(3, experience);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Erro ao salvar experiência de progressão.", exception);
        }
    }

    @Override
    public void addExperience(UUID playerUuid, ProgressionGame game, long amount) {
        if (amount <= 0L) {
            throw new IllegalArgumentException("Amount deve ser maior que zero.");
        }
        String sql = """
                INSERT INTO laboon_progression (player_uuid, game, experience)
                VALUES (?, ?, ?)
                ON CONFLICT (player_uuid, game)
                DO UPDATE SET experience = laboon_progression.experience + EXCLUDED.experience,
                              updated_at = CURRENT_TIMESTAMP
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, playerUuid);
            statement.setString(2, game.name());
            statement.setLong(3, amount);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Erro ao adicionar experiência de progressão.", exception);
        }
    }
}
