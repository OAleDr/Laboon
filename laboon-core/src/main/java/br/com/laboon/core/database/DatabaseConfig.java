package br.com.laboon.core.database;

public final class DatabaseConfig {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private final int maximumPoolSize;
    private final int minimumIdle;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;

    public DatabaseConfig(
            String host,
            int port,
            String database,
            String username,
            String password,
            int maximumPoolSize,
            int minimumIdle,
            long connectionTimeout,
            long idleTimeout,
            long maxLifetime
    ) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maximumPoolSize = maximumPoolSize;
        this.minimumIdle = minimumIdle;
        this.connectionTimeout = connectionTimeout;
        this.idleTimeout = idleTimeout;
        this.maxLifetime = maxLifetime;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://"
                + host
                + ":"
                + port
                + "/"
                + database;
    }
}