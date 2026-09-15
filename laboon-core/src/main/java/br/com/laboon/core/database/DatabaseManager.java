package br.com.laboon.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ServiceLoader;

public final class DatabaseManager {

    private final HikariDataSource dataSource;

    public DatabaseManager(DatabaseConfig config) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "DatabaseConfig não pode ser nulo."
            );
        }

        System.out.println("===== LABOON DATABASE DEBUG =====");
        System.out.println("JDBC URL: " + config.getJdbcUrl());

        try {
            Class<?> driverClass = Class.forName("org.postgresql.Driver");

            System.out.println(
                    "PostgreSQL Driver encontrado: "
                            + driverClass.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
            );

            Driver driver = (Driver) driverClass
                    .getDeclaredConstructor()
                    .newInstance();

            DriverManager.registerDriver(
                    new DriverShim(driver)
            );

            System.out.println("PostgreSQL Driver registrado com sucesso.");

        } catch (Exception exception) {

            System.err.println(
                    "ERRO AO CARREGAR POSTGRESQL DRIVER"
            );

            exception.printStackTrace();

            throw new IllegalStateException(
                    "PostgreSQL JDBC Driver não pôde ser carregado.",
                    exception
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

    private static final class DriverShim implements Driver {

        private final Driver delegate;

        private DriverShim(Driver delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection connect(
                String url,
                java.util.Properties info
        ) throws SQLException {
            return delegate.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url)
                throws SQLException {
            return delegate.acceptsURL(url);
        }

        @Override
        public java.sql.DriverPropertyInfo[] getPropertyInfo(
                String url,
                java.util.Properties info
        ) throws SQLException {
            return delegate.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegate.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() {
            try {
                return delegate.getParentLogger();
            } catch (Exception exception) {
                return java.util.logging.Logger.getGlobal();
            }
        }
    }
}