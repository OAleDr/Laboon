package br.com.laboon.core.account.repository;

import br.com.laboon.core.account.AccountPreferences;
import br.com.laboon.core.language.LanguageLocale;
import br.com.laboon.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class PostgreSqlAccountPreferencesRepository {

    private final DatabaseManager database;

    public PostgreSqlAccountPreferencesRepository(
            DatabaseManager database
    ) {

        if (database == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager não pode ser nulo."
            );
        }

        this.database = database;
    }

    public AccountPreferences findByUniqueId(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        final String sql = """
                SELECT
                    "language",
                    "privateMessages",
                    "friendRequests",
                    "serverJoinMessages"
                FROM "account_preferences"
                WHERE "uniqueId" = ?
                LIMIT 1
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            /*
             * Neste novo modelo vamos usar diretamente
             * UUID como uniqueId.
             *
             * Se a tabela ainda estiver ligada ao antigo
             * users.id, ela deverá ser recriada.
             */
            statement.setString(
                    1,
                    uniqueId.toString()
            );

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return map(resultSet);
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao buscar preferências: "
                            + uniqueId,
                    exception
            );
        }
    }

    public AccountPreferences findOrCreate(UUID uniqueId) {

        AccountPreferences preferences =
                findByUniqueId(uniqueId);

        if (preferences != null) {
            return preferences;
        }

        preferences = new AccountPreferences();

        save(
                uniqueId,
                preferences
        );

        return preferences;
    }

    public void save(
            UUID uniqueId,
            AccountPreferences preferences
    ) {

        if (uniqueId == null) {
            throw new IllegalArgumentException(
                    "UUID não pode ser nulo."
            );
        }

        if (preferences == null) {
            throw new IllegalArgumentException(
                    "Preferences não podem ser nulas."
            );
        }

        final String sql = """
                INSERT INTO "account_preferences" (
                    "uniqueId",
                    "language",
                    "privateMessages",
                    "friendRequests",
                    "serverJoinMessages"
                )
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT ("uniqueId")
                DO UPDATE SET
                    "language" =
                        EXCLUDED."language",
                    "privateMessages" =
                        EXCLUDED."privateMessages",
                    "friendRequests" =
                        EXCLUDED."friendRequests",
                    "serverJoinMessages" =
                        EXCLUDED."serverJoinMessages"
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
                    preferences.getLanguage().getCode()
            );

            statement.setBoolean(
                    3,
                    preferences.isPrivateMessages()
            );

            statement.setBoolean(
                    4,
                    preferences.isFriendRequests()
            );

            statement.setBoolean(
                    5,
                    preferences.isServerJoinMessages()
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao salvar preferências: "
                            + uniqueId,
                    exception
            );
        }
    }

    public void delete(UUID uniqueId) {

        if (uniqueId == null) {
            return;
        }

        final String sql = """
                DELETE FROM "account_preferences"
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
                    "Erro ao deletar preferências: "
                            + uniqueId,
                    exception
            );
        }
    }

    private AccountPreferences map(
            ResultSet resultSet
    ) throws SQLException {

        LanguageLocale language;

        try {

            language =
                    LanguageLocale.fromCode(
                            resultSet.getString(
                                    "language"
                            )
                    );

        } catch (Exception exception) {

            language =
                    LanguageLocale.ptBR();
        }

        return new AccountPreferences(
                language,
                resultSet.getBoolean(
                        "privateMessages"
                ),
                resultSet.getBoolean(
                        "friendRequests"
                ),
                resultSet.getBoolean(
                        "serverJoinMessages"
                )
        );
    }
}