package br.com.laboon.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseManager {

    private final HikariDataSource dataSource;

    public DatabaseManager(DatabaseConfig config) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "DatabaseConfig não pode ser nulo."
            );
        }

        HikariConfig hikari = new HikariConfig();

        hikari.setJdbcUrl(config.getJdbcUrl());
        hikari.setUsername(config.getUsername());
        hikari.setPassword(config.getPassword());

        hikari.setMaximumPoolSize(config.getMaximumPoolSize());
        hikari.setMinimumIdle(config.getMinimumIdle());

        hikari.setConnectionTimeout(config.getConnectionTimeout());
        hikari.setIdleTimeout(config.getIdleTimeout());
        hikari.setMaxLifetime(config.getMaxLifetime());

        hikari.setPoolName("Laboon-PostgreSQL");

        hikari.setAutoCommit(true);

        this.dataSource = new HikariDataSource(hikari);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean isConnected() {

        try (Connection connection = getConnection()) {
            return connection.isValid(2);
        } catch (SQLException exception) {
            return false;
        }
    }

    public void close() {
        dataSource.close();
    }
}