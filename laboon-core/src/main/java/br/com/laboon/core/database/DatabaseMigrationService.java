package br.com.laboon.core.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseMigrationService {

    private final DatabaseManager databaseManager;

    public DatabaseMigrationService(
            DatabaseManager databaseManager
    ) {

        if (databaseManager == null) {
            throw new IllegalArgumentException(
                    "DatabaseManager não pode ser nulo."
            );
        }

        this.databaseManager =
                databaseManager;
    }

    public void execute(
            String resourcePath
    ) {

        if (
                resourcePath == null
                        || resourcePath.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Resource path não pode ser vazio."
            );
        }

        String sql =
                loadResource(
                        resourcePath
                );

        if (sql.isBlank()) {
            return;
        }

        try (
                Connection connection =
                        databaseManager.getConnection()
        ) {

            executeStatements(
                    connection,
                    sql
            );

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao executar migration: "
                            + resourcePath,
                    exception
            );
        }
    }

    private String loadResource(
            String resourcePath
    ) {

        ClassLoader classLoader =
                DatabaseMigrationService.class
                        .getClassLoader();

        try (
                InputStream inputStream =
                        classLoader.getResourceAsStream(
                                resourcePath
                        )
        ) {

            if (inputStream == null) {

                throw new IllegalStateException(
                        "Migration não encontrada: "
                                + resourcePath
                );
            }

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            inputStream,
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {

                StringBuilder builder =
                        new StringBuilder();

                String line;

                while (
                        (line = reader.readLine())
                                != null
                ) {

                    builder
                            .append(line)
                            .append('\n');
                }

                return builder.toString();
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Erro ao ler migration: "
                            + resourcePath,
                    exception
            );
        }
    }

    private void executeStatements(
            Connection connection,
            String sql
    ) throws SQLException {

        String[] statements =
                sql.split(
                        ";\\s*(?:\\r?\\n|$)"
                );

        for (
                String statementSql :
                statements
        ) {

            String statement =
                    statementSql.trim();

            if (statement.isBlank()) {
                continue;
            }

            try (
                    var sqlStatement =
                            connection.createStatement()
            ) {

                sqlStatement.execute(
                        statement
                );
            }
        }
    }
}