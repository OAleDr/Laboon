package br.com.laboon.core.rewards;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountPreferences;
import br.com.laboon.core.account.AccountType;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.database.DatabaseManager;
import br.com.laboon.core.economy.EconomyCurrency;
import br.com.laboon.core.economy.EconomyTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public final class PostgreSqlRewardTransactionRepository
        implements RewardTransactionRepository {

    private final DatabaseManager database;

    public PostgreSqlRewardTransactionRepository(
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
    public boolean tryClaim(
            Connection connection,
            String rewardId,
            UUID playerUuid,
            RewardSource source
    ) {

        String sql = """
                INSERT INTO laboon_reward_claims (
                    reward_id,
                    player_uuid,
                    source
                )
                VALUES (?, ?, ?)
                ON CONFLICT (reward_id)
                DO NOTHING
                """;

        try (
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
                    source.name()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao registrar reward claim.",
                    exception
            );
        }
    }

    @Override
    public Account findAccount(
            Connection connection,
            UUID playerUuid
    ) {

        String sql = """
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
                LIMIT 1
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
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {
                    return null;
                }

                UUID uniqueId =
                        resultSet.getObject(
                                "uniqueId",
                                UUID.class
                        );

                String name =
                        resultSet.getString(
                                "name"
                        );

                Group group =
                        parseGroup(
                                resultSet.getString(
                                        "group"
                                )
                        );

                String tag =
                        resultSet.getString(
                                "tag"
                        );

                long experience =
                        resultSet.getLong(
                                "experience"
                        );

                AccountType type =
                        parseAccountType(
                                resultSet.getString(
                                        "type"
                                )
                        );

                Timestamp createdTimestamp =
                        resultSet.getTimestamp(
                                "createdAt"
                        );

                Timestamp lastLoginTimestamp =
                        resultSet.getTimestamp(
                                "lastLogin"
                        );

                Account account =
                        new Account(
                                uniqueId,
                                name,
                                type,
                                createdTimestamp != null
                                        ? createdTimestamp.toInstant()
                                        : java.time.Instant.now(),
                                lastLoginTimestamp != null
                                        ? lastLoginTimestamp.toInstant()
                                        : null,
                                new AccountPreferences()
                        );

                account.setGroup(group);
                account.setTag(tag);
                account.setExperience(experience);

                return account;
            }

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao buscar Account.",
                    exception
            );
        }
    }

    @Override
    public void saveAccount(
            Connection connection,
            Account account
    ) {

        String sql = """
                UPDATE "accounts"
                SET
                    "experience" = ?
                WHERE "uniqueId" = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    account.getExperience()
            );

            statement.setObject(
                    2,
                    account.getUniqueId()
            );

            int updated =
                    statement.executeUpdate();

            if (updated == 0) {

                throw new IllegalStateException(
                        "Account não encontrada: "
                                + account.getUniqueId()
                );
            }

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao salvar XP.",
                    exception
            );
        }
    }

    @Override
    public void updateEconomy(
            Connection connection,
            UUID playerUuid,
            EconomyCurrency currency,
            long amount
    ) {

        String column;

        switch (currency) {

            case COINS:
                column = "coins";
                break;

            case TOKENS:
                column = "tokens";
                break;

            default:
                throw new IllegalArgumentException(
                        "Currency não suportada: "
                                + currency
                );
        }

        String sql = """
                INSERT INTO laboon_economy_accounts (
                    player_uuid,
                    %s
                )
                VALUES (?, ?)
                ON CONFLICT (player_uuid)
                DO UPDATE SET
                    %s =
                        laboon_economy_accounts.%s
                        + EXCLUDED.%s,
                    updated_at =
                        CURRENT_TIMESTAMP
                """.formatted(
                column,
                column,
                column,
                column
        );

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    playerUuid
            );

            statement.setLong(
                    2,
                    amount
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao atualizar economia.",
                    exception
            );
        }
    }

    @Override
    public void saveEconomyTransaction(
            Connection connection,
            EconomyTransaction transaction
    ) {

        String sql = """
                INSERT INTO laboon_economy_transactions (
                    id,
                    player_uuid,
                    currency,
                    amount,
                    type,
                    source,
                    metadata,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    transaction.getId()
            );

            statement.setObject(
                    2,
                    transaction.getPlayerUuid()
            );

            statement.setString(
                    3,
                    transaction.getCurrency().name()
            );

            statement.setLong(
                    4,
                    transaction.getAmount()
            );

            statement.setString(
                    5,
                    transaction.getType().name()
            );

            statement.setString(
                    6,
                    transaction.getSource()
            );

            statement.setString(
                    7,
                    transaction.getMetadata()
            );

            if (transaction.getCreatedAt() == null) {

                statement.setTimestamp(
                        8,
                        Timestamp.from(
                                java.time.Instant.now()
                        )
                );

            } else {

                statement.setTimestamp(
                        8,
                        Timestamp.from(
                                transaction.getCreatedAt()
                        )
                );
            }

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao salvar EconomyTransaction.",
                    exception
            );
        }
    }

    private Group parseGroup(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            return Group.DEFAULT;
        }

        try {

            return Group.valueOf(
                    value
            );

        } catch (IllegalArgumentException exception) {

            return Group.DEFAULT;
        }
    }

    private AccountType parseAccountType(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            return AccountType.ORIGINAL;
        }

        try {

            return AccountType.valueOf(
                    value
            );

        } catch (IllegalArgumentException exception) {

            return AccountType.ORIGINAL;
        }
    }
}