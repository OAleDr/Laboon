package br.com.laboon.core.economy;

import br.com.laboon.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PostgreSqlEconomyRepository implements EconomyRepository {

    private final DatabaseManager databaseManager;

    public PostgreSqlEconomyRepository(
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
    public EconomyAccount find(
            UUID playerUuid
    ) {

        validateUuid(playerUuid);

        String sql = """
                SELECT coins, tokens
                FROM laboon_economy_accounts
                WHERE player_uuid = ?
                """;

        try (
                Connection connection =
                        databaseManager.getConnection();

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

                EconomyAccount account =
                        new EconomyAccount(
                                playerUuid
                        );

                account.setBalance(
                        EconomyCurrency.COINS,
                        resultSet.getLong("coins")
                );

                account.setBalance(
                        EconomyCurrency.TOKENS,
                        resultSet.getLong("tokens")
                );

                return account;
            }

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao buscar economia do jogador: "
                            + playerUuid,
                    exception
            );
        }
    }

    @Override
    public EconomyAccount getOrCreate(
            UUID playerUuid
    ) {

        validateUuid(playerUuid);

        EconomyAccount existing =
                find(playerUuid);

        if (existing != null) {
            return existing;
        }

        String sql = """
                INSERT INTO laboon_economy_accounts
                    (
                        player_uuid,
                        coins,
                        tokens,
                        updated_at
                    )
                VALUES
                    (?, 0, 0, ?)
                ON CONFLICT (player_uuid)
                DO NOTHING
                """;

        try (
                Connection connection =
                        databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    playerUuid
            );

            statement.setTimestamp(
                    2,
                    Timestamp.from(Instant.now())
            );

            statement.executeUpdate();

            return find(playerUuid);

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao criar conta de economia: "
                            + playerUuid,
                    exception
            );
        }
    }

    @Override
    public void save(
            EconomyAccount account
    ) {

        if (account == null) {
            throw new IllegalArgumentException(
                    "EconomyAccount não pode ser nulo."
            );
        }

        String sql = """
                INSERT INTO laboon_economy_accounts
                    (
                        player_uuid,
                        coins,
                        tokens,
                        updated_at
                    )
                VALUES
                    (?, ?, ?, ?)
                ON CONFLICT (player_uuid)
                DO UPDATE SET
                    coins = EXCLUDED.coins,
                    tokens = EXCLUDED.tokens,
                    updated_at = EXCLUDED.updated_at
                """;

        try (
                Connection connection =
                        databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    account.getPlayerUuid()
            );

            statement.setLong(
                    2,
                    account.getBalance(
                            EconomyCurrency.COINS
                    )
            );

            statement.setLong(
                    3,
                    account.getBalance(
                            EconomyCurrency.TOKENS
                    )
            );

            statement.setTimestamp(
                    4,
                    Timestamp.from(
                            Instant.now()
                    )
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao salvar economia do jogador: "
                            + account.getPlayerUuid(),
                    exception
            );
        }
    }

    @Override
    public boolean exists(
            UUID playerUuid
    ) {

        validateUuid(playerUuid);

        String sql = """
                SELECT 1
                FROM laboon_economy_accounts
                WHERE player_uuid = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        databaseManager.getConnection();

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

                return resultSet.next();
            }

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao verificar economia do jogador: "
                            + playerUuid,
                    exception
            );
        }
    }

    @Override
    public void delete(
            UUID playerUuid
    ) {

        validateUuid(playerUuid);

        String deleteTransactions = """
                DELETE FROM laboon_economy_transactions
                WHERE player_uuid = ?
                """;

        String deleteAccount = """
                DELETE FROM laboon_economy_accounts
                WHERE player_uuid = ?
                """;

        try (
                Connection connection =
                        databaseManager.getConnection()
        ) {

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    deleteTransactions
                            )
            ) {

                statement.setObject(
                        1,
                        playerUuid
                );

                statement.executeUpdate();
            }

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    deleteAccount
                            )
            ) {

                statement.setObject(
                        1,
                        playerUuid
                );

                statement.executeUpdate();
            }

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao remover economia do jogador: "
                            + playerUuid,
                    exception
            );
        }
    }

    @Override
    public void saveTransaction(
            EconomyTransaction transaction
    ) {

        if (transaction == null) {
            throw new IllegalArgumentException(
                    "EconomyTransaction não pode ser nula."
            );
        }

        String sql = """
                INSERT INTO laboon_economy_transactions
                    (
                        id,
                        player_uuid,
                        currency,
                        amount,
                        type,
                        source,
                        metadata,
                        created_at
                    )
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        databaseManager.getConnection();

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
                    transaction
                            .getCurrency()
                            .name()
            );

            statement.setLong(
                    4,
                    transaction.getAmount()
            );

            statement.setString(
                    5,
                    transaction
                            .getType()
                            .name()
            );

            statement.setString(
                    6,
                    transaction.getSource()
            );

            statement.setString(
                    7,
                    transaction.getMetadata()
            );

            statement.setTimestamp(
                    8,
                    Timestamp.from(
                            transaction.getCreatedAt()
                    )
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao salvar transação de economia: "
                            + transaction.getId(),
                    exception
            );
        }
    }

    @Override
    public List<EconomyTransaction> getTransactions(
            UUID playerUuid,
            int limit
    ) {

        validateUuid(playerUuid);

        int safeLimit =
                Math.max(
                        1,
                        Math.min(limit, 100)
                );

        String sql = """
                SELECT
                    id,
                    currency,
                    amount,
                    type,
                    source,
                    metadata,
                    created_at
                FROM laboon_economy_transactions
                WHERE player_uuid = ?
                ORDER BY created_at DESC
                LIMIT ?
                """;

        List<EconomyTransaction> transactions =
                new ArrayList<>();

        try (
                Connection connection =
                        databaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setObject(
                    1,
                    playerUuid
            );

            statement.setInt(
                    2,
                    safeLimit
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    UUID id =
                            resultSet.getObject(
                                    "id",
                                    UUID.class
                            );

                    EconomyCurrency currency =
                            parseCurrency(
                                    resultSet.getString(
                                            "currency"
                                    )
                            );

                    EconomyTransactionType type =
                            parseTransactionType(
                                    resultSet.getString(
                                            "type"
                                    )
                            );

                    Timestamp timestamp =
                            resultSet.getTimestamp(
                                    "created_at"
                            );

                    Instant createdAt =
                            timestamp != null
                                    ? timestamp.toInstant()
                                    : Instant.now();

                    transactions.add(
                            new EconomyTransaction(
                                    id,
                                    playerUuid,
                                    currency,
                                    resultSet.getLong(
                                            "amount"
                                    ),
                                    type,
                                    resultSet.getString(
                                            "source"
                                    ),
                                    resultSet.getString(
                                            "metadata"
                                    ),
                                    createdAt
                            )
                    );
                }
            }

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao buscar histórico de economia: "
                            + playerUuid,
                    exception
            );
        }

        return transactions;
    }

    private EconomyCurrency parseCurrency(
            String value
    ) {

        if (value == null) {
            throw new IllegalStateException(
                    "Currency inválida no banco."
            );
        }

        try {
            return EconomyCurrency.valueOf(
                    value.toUpperCase()
            );
        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "Currency inválida no banco: "
                            + value,
                    exception
            );
        }
    }

    private EconomyTransactionType parseTransactionType(
            String value
    ) {

        if (value == null) {
            throw new IllegalStateException(
                    "Transaction type inválido no banco."
            );
        }

        try {
            return EconomyTransactionType.valueOf(
                    value.toUpperCase()
            );
        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "Transaction type inválido no banco: "
                            + value,
                    exception
            );
        }
    }

    private void validateUuid(
            UUID playerUuid
    ) {

        if (playerUuid == null) {
            throw new IllegalArgumentException(
                    "UUID não pode ser nulo."
            );
        }
    }
}