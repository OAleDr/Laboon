package br.com.laboon.core.account.repository;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountPreferences;
import br.com.laboon.core.account.AccountType;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.account.punishment.Ban;
import br.com.laboon.core.account.punishment.Kick;
import br.com.laboon.core.account.punishment.Mute;
import br.com.laboon.core.account.punishment.PunishmentHistory;
import br.com.laboon.core.language.LanguageLocale;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;

public final class PostgreSqlAccountRepository
        implements AccountRepository {

    private final DataSource dataSource;

    public PostgreSqlAccountRepository(DataSource dataSource) {

        if (dataSource == null) {
            throw new IllegalArgumentException(
                    "DataSource não pode ser nulo."
            );
        }

        this.dataSource = dataSource;
    }

    @Override
    public Account findById(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        String sql = """
                SELECT
                    id,
                    uuid,
                    name,
                    group_name,
                    tag,
                    experience,
                    type,
                    created_at,
                    last_login,
                    language,
                    private_messages,
                    friend_requests,
                    server_join_messages
                FROM accounts
                WHERE uuid = ?
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setObject(1, uniqueId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                Account account = mapAccount(result);

                loadTemporaryGroups(connection, account);
                loadPunishments(connection, account);

                return account;
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao carregar account " + uniqueId,
                    exception
            );
        }
    }

    @Override
    public Account findByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        String sql = """
                SELECT
                    id,
                    uuid,
                    name,
                    group_name,
                    tag,
                    experience,
                    type,
                    created_at,
                    last_login,
                    language,
                    private_messages,
                    friend_requests,
                    server_join_messages
                FROM accounts
                WHERE LOWER(name) = LOWER(?)
                LIMIT 1
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, name.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                Account account = mapAccount(result);

                loadTemporaryGroups(connection, account);
                loadPunishments(connection, account);

                return account;
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao buscar account pelo nome " + name,
                    exception
            );
        }
    }

    @Override
    public boolean exists(UUID uniqueId) {

        if (uniqueId == null) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM accounts
                WHERE uuid = ?
                LIMIT 1
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setObject(1, uniqueId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao verificar existência da account.",
                    exception
            );
        }
    }

    @Override
    public void insert(Account account) {

        String sql = """
                INSERT INTO accounts (
                    uuid,
                    name,
                    group_name,
                    tag,
                    experience,
                    type,
                    created_at,
                    last_login,
                    language,
                    private_messages,
                    friend_requests,
                    server_join_messages
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            writeAccount(statement, account);

            statement.executeUpdate();

            replaceTemporaryGroups(connection, account);
            replacePunishments(connection, account);

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao inserir account "
                            + account.getUniqueId(),
                    exception
            );
        }
    }

    @Override
    public void update(Account account) {

        String sql = """
                UPDATE accounts
                SET
                    name = ?,
                    group_name = ?,
                    tag = ?,
                    experience = ?,
                    type = ?,
                    last_login = ?,
                    language = ?,
                    private_messages = ?,
                    friend_requests = ?,
                    server_join_messages = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE uuid = ?
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            writeUpdate(statement, account);

            statement.executeUpdate();

            replaceTemporaryGroups(connection, account);
            replacePunishments(connection, account);

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao atualizar account "
                            + account.getUniqueId(),
                    exception
            );
        }
    }

    @Override
    public void delete(UUID uniqueId) {

        if (uniqueId == null) {
            return;
        }

        String sql = """
                DELETE FROM accounts
                WHERE uuid = ?
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setObject(1, uniqueId);
            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao remover account.",
                    exception
            );
        }
    }

    private Account mapAccount(ResultSet result)
            throws SQLException {

        UUID uuid = result.getObject(
                "uuid",
                UUID.class
        );

        String name = result.getString("name");

        AccountType type;

        try {
            type = AccountType.valueOf(
                    result.getString("type")
            );
        } catch (Exception exception) {
            type = AccountType.ORIGINAL;
        }

        Instant createdAt =
                result.getTimestamp("created_at")
                        .toInstant();

        Timestamp lastLoginTimestamp =
                result.getTimestamp("last_login");

        Instant lastLogin =
                lastLoginTimestamp == null
                        ? null
                        : lastLoginTimestamp.toInstant();

        AccountPreferences preferences =
                new AccountPreferences();

        String language =
                result.getString("language");

        if (language != null && !language.isBlank()) {
            preferences.setLanguage(LanguageLocale.fromCode(language));
        }

        preferences.setPrivateMessages(
                result.getBoolean("private_messages")
        );

        preferences.setFriendRequests(
                result.getBoolean("friend_requests")
        );

        preferences.setServerJoinMessages(
                result.getBoolean("server_join_messages")
        );

        Account account = new Account(
                uuid,
                name,
                type,
                createdAt,
                lastLogin,
                preferences
        );

        String groupName =
                result.getString("group_name");

        if (groupName != null) {

            try {
                account.setGroup(
                        Group.valueOf(groupName)
                );
            } catch (IllegalArgumentException ignored) {
            }
        }

        account.setTag(
                result.getString("tag")
        );

        account.setExperience(
                result.getLong("experience")
        );

        return account;
    }

    private void writeAccount(
            PreparedStatement statement,
            Account account
    ) throws SQLException {

        AccountPreferences preferences =
                account.getPreferences();

        statement.setObject(
                1,
                account.getUniqueId()
        );

        statement.setString(
                2,
                account.getName()
        );

        statement.setString(
                3,
                account.getGroup().name()
        );

        statement.setString(
                4,
                account.getTag()
        );

        statement.setLong(
                5,
                account.getExperience()
        );

        statement.setString(
                6,
                account.getType().name()
        );

        statement.setTimestamp(
                7,
                Timestamp.from(account.getCreatedAt())
        );

        if (account.getLastLogin() == null) {
            statement.setNull(8, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(
                    8,
                    Timestamp.from(
                            account.getLastLogin()
                    )
            );
        }

        statement.setString(
                9,
                preferences.getLanguage().getCode()
        );

        statement.setBoolean(
                10,
                preferences.isPrivateMessages()
        );

        statement.setBoolean(
                11,
                preferences.isFriendRequests()
        );

        statement.setBoolean(
                12,
                preferences.isServerJoinMessages()
        );
    }

    private void writeUpdate(
            PreparedStatement statement,
            Account account
    ) throws SQLException {

        AccountPreferences preferences =
                account.getPreferences();

        statement.setString(1, account.getName());
        statement.setString(2, account.getGroup().name());
        statement.setString(3, account.getTag());
        statement.setLong(4, account.getExperience());
        statement.setString(5, account.getType().name());

        if (account.getLastLogin() == null) {
            statement.setNull(6, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(
                    6,
                    Timestamp.from(
                            account.getLastLogin()
                    )
            );
        }

        statement.setString(
                7,
                preferences.getLanguage().getCode()
        );

        statement.setBoolean(
                8,
                preferences.isPrivateMessages()
        );

        statement.setBoolean(
                9,
                preferences.isFriendRequests()
        );

        statement.setBoolean(
                10,
                preferences.isServerJoinMessages()
        );

        statement.setObject(
                11,
                account.getUniqueId()
        );
    }

    private void loadTemporaryGroups(
            Connection connection,
            Account account
    ) throws SQLException {

        String sql = """
                SELECT group_name, expires_at
                FROM account_temporary_groups
                WHERE account_id = (
                    SELECT id
                    FROM accounts
                    WHERE uuid = ?
                )
                AND expires_at > CURRENT_TIMESTAMP
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    account.getUniqueId()
            );

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    try {

                        Group group =
                                Group.valueOf(
                                        result.getString(
                                                "group_name"
                                        )
                                );

                        Instant expiresAt =
                                result.getTimestamp(
                                        "expires_at"
                                ).toInstant();

                        account.setTemporaryGroup(
                                group,
                                expiresAt
                        );

                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }
    }

    private void replaceTemporaryGroups(
            Connection connection,
            Account account
    ) throws SQLException {

        String delete = """
                DELETE FROM account_temporary_groups
                WHERE account_id = (
                    SELECT id
                    FROM accounts
                    WHERE uuid = ?
                )
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(delete)
        ) {

            statement.setObject(
                    1,
                    account.getUniqueId()
            );

            statement.executeUpdate();
        }

        String insert = """
                INSERT INTO account_temporary_groups (
                    account_id,
                    group_name,
                    expires_at
                )
                SELECT
                    id,
                    ?,
                    ?
                FROM accounts
                WHERE uuid = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(insert)
        ) {

            for (var entry :
                    account.getTemporaryGroups().entrySet()) {

                statement.setString(
                        1,
                        entry.getKey().name()
                );

                statement.setTimestamp(
                        2,
                        Timestamp.from(
                                entry.getValue()
                        )
                );

                statement.setObject(
                        3,
                        account.getUniqueId()
                );

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void loadPunishments(
            Connection connection,
            Account account
    ) throws SQLException {

        String sql = """
                SELECT *
                FROM punishments
                WHERE account_id = (
                    SELECT id
                    FROM accounts
                    WHERE uuid = ?
                )
                ORDER BY created_at ASC
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    account.getUniqueId()
            );

            try (ResultSet result =
                         statement.executeQuery()) {

                PunishmentHistory history =
                        new PunishmentHistory();

                /*
                 * O carregamento individual das punições
                 * será implementado aqui usando os
                 * métodos restore() das classes existentes.
                 */

                while (result.next()) {

                    // reservado para Ban/Mute/Kick
                }

                account.setPunishmentHistory(history);
            }
        }
    }

    private void replacePunishments(
            Connection connection,
            Account account
    ) throws SQLException {

        String delete = """
                DELETE FROM punishments
                WHERE account_id = (
                    SELECT id
                    FROM accounts
                    WHERE uuid = ?
                )
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(delete)
        ) {

            statement.setObject(
                    1,
                    account.getUniqueId()
            );

            statement.executeUpdate();
        }

        /*
         * Inserção das punições será feita usando
         * as listas existentes no PunishmentHistory.
         */
    }
}