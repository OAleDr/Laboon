package br.com.laboon.core.account.repository;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountType;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;

public final class PostgreSqlAccountRepository implements AccountRepository {

    private final DatabaseManager database;

    public PostgreSqlAccountRepository(DatabaseManager database) {
        this.database = database;
    }

    /**
     * Busca uma Account pelo UUID do Minecraft.
     */
    public Account findByUniqueId(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        final String sql = """
                SELECT
                    "uniqueId",
                    "name",
                    "group",
                    "tag",
                    "experience",
                    "type",
                    "createdAt",
                    "lastLogin"
                FROM "accounts"
                WHERE "uniqueId" = ?
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setObject(1, uniqueId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapAccount(resultSet);
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao buscar Account: " + uniqueId,
                    exception
            );
        }
    }

    /**
     * Compatibilidade com chamadas usando findById.
     */
    public Account findById(UUID uniqueId) {
        return findByUniqueId(uniqueId);
    }

    /**
     * Busca uma Account pelo nome.
     */
    public Account findByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        final String sql = """
                SELECT
                    "uniqueId",
                    "name",
                    "group",
                    "tag",
                    "experience",
                    "type",
                    "createdAt",
                    "lastLogin"
                FROM "accounts"
                WHERE LOWER("name") = LOWER(?)
                LIMIT 1
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapAccount(resultSet);
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao buscar Account pelo nome: " + name,
                    exception
            );
        }
    }

    /**
     * Verifica se a Account existe.
     */
    public boolean exists(UUID uniqueId) {

        if (uniqueId == null) {
            return false;
        }

        final String sql = """
                SELECT 1
                FROM "accounts"
                WHERE "uniqueId" = ?
                LIMIT 1
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setObject(1, uniqueId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao verificar Account: " + uniqueId,
                    exception
            );
        }
    }

    /**
     * Insere uma nova Account.
     */
    public void insert(Account account) {

        validateAccount(account);

        final String sql = """
                INSERT INTO "accounts" (
                    "uniqueId",
                    "name",
                    "group",
                    "tag",
                    "experience",
                    "type",
                    "createdAt",
                    "lastLogin"
                )
                VALUES (
                    ?,
                    ?,
                    ?::"Group",
                    ?,
                    ?,
                    ?::"AccountType",
                    ?,
                    ?
                )
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setObject(1, account.getUniqueId());
            statement.setString(2, account.getName());
            statement.setString(3, account.getGroup().name());
            statement.setString(4, account.getTag());
            statement.setLong(5, account.getExperience());
            statement.setString(6, account.getType().name());
            statement.setTimestamp(
                    7,
                    Timestamp.from(account.getCreatedAt())
            );

            if (account.getLastLogin() == null) {
                statement.setNull(8, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(
                        8,
                        Timestamp.from(account.getLastLogin())
                );
            }

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao inserir Account: " + account.getUniqueId(),
                    exception
            );
        }
    }

    /**
     * Atualiza uma Account existente.
     */
    public void update(Account account) {

        validateAccount(account);

        final String sql = """
                UPDATE "accounts"
                SET
                    "name" = ?,
                    "group" = ?::"Group",
                    "tag" = ?,
                    "experience" = ?,
                    "type" = ?::"AccountType",
                    "lastLogin" = ?
                WHERE "uniqueId" = ?
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

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
                        Timestamp.from(account.getLastLogin())
                );
            }

            statement.setObject(7, account.getUniqueId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new IllegalStateException(
                        "Account não encontrada para atualização: "
                                + account.getUniqueId()
                );
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao atualizar Account: " + account.getUniqueId(),
                    exception
            );
        }
    }

    /**
     * Salva a Account.
     *
     * Cria caso ainda não exista.
     * Atualiza caso já exista.
     */
    public void save(Account account) {

        validateAccount(account);

        final String sql = """
                INSERT INTO "accounts" (
                    "uniqueId",
                    "name",
                    "group",
                    "tag",
                    "experience",
                    "type",
                    "createdAt",
                    "lastLogin"
                )
                VALUES (
                    ?,
                    ?,
                    ?::"Group",
                    ?,
                    ?,
                    ?::"AccountType",
                    ?,
                    ?
                )
                ON CONFLICT ("uniqueId")
                DO UPDATE SET
                    "name" = EXCLUDED."name",
                    "group" = EXCLUDED."group",
                    "tag" = EXCLUDED."tag",
                    "experience" = EXCLUDED."experience",
                    "type" = EXCLUDED."type",
                    "lastLogin" = EXCLUDED."lastLogin"
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setObject(1, account.getUniqueId());
            statement.setString(2, account.getName());
            statement.setString(3, account.getGroup().name());
            statement.setString(4, account.getTag());
            statement.setLong(5, account.getExperience());
            statement.setString(6, account.getType().name());
            statement.setTimestamp(
                    7,
                    Timestamp.from(account.getCreatedAt())
            );

            if (account.getLastLogin() == null) {
                statement.setNull(8, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(
                        8,
                        Timestamp.from(account.getLastLogin())
                );
            }

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao salvar Account: " + account.getUniqueId(),
                    exception
            );
        }
    }

    /**
     * Remove uma Account.
     */
    public void delete(UUID uniqueId) {

        if (uniqueId == null) {
            return;
        }

        final String sql = """
                DELETE FROM "accounts"
                WHERE "uniqueId" = ?
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setObject(1, uniqueId);
            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao deletar Account: " + uniqueId,
                    exception
            );
        }
    }

    /**
     * Atualiza somente o lastLogin.
     */
    public void updateLastLogin(UUID uniqueId, Instant lastLogin) {

        if (uniqueId == null) {
            return;
        }

        final String sql = """
                UPDATE "accounts"
                SET "lastLogin" = ?
                WHERE "uniqueId" = ?
                """;

        try (
                Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            if (lastLogin == null) {
                statement.setNull(1, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(
                        1,
                        Timestamp.from(lastLogin)
                );
            }

            statement.setObject(2, uniqueId);

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Erro ao atualizar lastLogin: " + uniqueId,
                    exception
            );
        }
    }

    /**
     * Cria uma Account padrão.
     */
    public Account create(UUID uniqueId, String name) {

        if (uniqueId == null) {
            throw new IllegalArgumentException(
                    "uniqueId não pode ser null"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "name não pode ser vazio"
            );
        }

        Account account = new Account(
                uniqueId,
                name
        );

        save(account);

        return account;
    }

    /**
     * Converte ResultSet para Account.
     */
    private Account mapAccount(ResultSet resultSet) throws SQLException {

        UUID uniqueId = resultSet.getObject(
                "uniqueId",
                UUID.class
        );

        String name = resultSet.getString("name");

        Group group = parseGroup(
                resultSet.getString("group")
        );

        String tag = resultSet.getString("tag");

        long experience = resultSet.getLong("experience");

        AccountType type = parseAccountType(
                resultSet.getString("type")
        );

        Timestamp createdTimestamp =
                resultSet.getTimestamp("createdAt");

        Timestamp lastLoginTimestamp =
                resultSet.getTimestamp("lastLogin");

        Instant createdAt =
                createdTimestamp != null
                        ? createdTimestamp.toInstant()
                        : Instant.now();

        Instant lastLogin =
                lastLoginTimestamp != null
                        ? lastLoginTimestamp.toInstant()
                        : null;

        Account account = new Account(
                uniqueId,
                name,
                type,
                createdAt,
                lastLogin,
                new br.com.laboon.core.account.AccountPreferences()
        );

        account.setGroup(group);
        account.setTag(tag);
        account.setExperience(experience);

        return account;
    }

    private Group parseGroup(String value) {

        if (value == null || value.isBlank()) {
            return Group.DEFAULT;
        }

        try {
            return Group.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return Group.DEFAULT;
        }
    }

    private AccountType parseAccountType(String value) {

        if (value == null || value.isBlank()) {
            return AccountType.ORIGINAL;
        }

        try {
            return AccountType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return AccountType.ORIGINAL;
        }
    }

    private void validateAccount(Account account) {

        if (account == null) {
            throw new IllegalArgumentException(
                    "account não pode ser null"
            );
        }

        if (account.getUniqueId() == null) {
            throw new IllegalArgumentException(
                    "Account.uniqueId não pode ser null"
            );
        }

        if (account.getName() == null ||
                account.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Account.name não pode ser vazio"
            );
        }

        if (account.getGroup() == null) {
            throw new IllegalArgumentException(
                    "Account.group não pode ser null"
            );
        }

        if (account.getType() == null) {
            throw new IllegalArgumentException(
                    "Account.type não pode ser null"
            );
        }

        if (account.getCreatedAt() == null) {
            throw new IllegalArgumentException(
                    "Account.createdAt não pode ser null"
            );
        }
    }
}