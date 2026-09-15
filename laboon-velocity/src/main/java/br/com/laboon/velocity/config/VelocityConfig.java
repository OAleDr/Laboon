package br.com.laboon.velocity.config;

public final class VelocityConfig {

    /*
     * =========================
     * REDIS
     * =========================
     */

    private final String redisHost;
    private final int redisPort;

    /*
     * =========================
     * PROXY
     * =========================
     */

    private final String proxyName;

    /*
     * =========================
     * POSTGRESQL
     * =========================
     */

    private final String databaseHost;
    private final int databasePort;
    private final String databaseName;
    private final String databaseUsername;
    private final String databasePassword;

    private final int databaseMaximumPoolSize;
    private final int databaseMinimumIdle;
    private final long databaseConnectionTimeout;
    private final long databaseIdleTimeout;
    private final long databaseMaxLifetime;

    public VelocityConfig(
            String redisHost,
            int redisPort,
            String proxyName,

            String databaseHost,
            int databasePort,
            String databaseName,
            String databaseUsername,
            String databasePassword,

            int databaseMaximumPoolSize,
            int databaseMinimumIdle,
            long databaseConnectionTimeout,
            long databaseIdleTimeout,
            long databaseMaxLifetime
    ) {

        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.proxyName = proxyName;

        this.databaseHost = databaseHost;
        this.databasePort = databasePort;
        this.databaseName = databaseName;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;

        this.databaseMaximumPoolSize = databaseMaximumPoolSize;
        this.databaseMinimumIdle = databaseMinimumIdle;
        this.databaseConnectionTimeout = databaseConnectionTimeout;
        this.databaseIdleTimeout = databaseIdleTimeout;
        this.databaseMaxLifetime = databaseMaxLifetime;
    }

    /*
     * =========================
     * REDIS
     * =========================
     */

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    /*
     * =========================
     * PROXY
     * =========================
     */

    public String getProxyName() {
        return proxyName;
    }

    /*
     * =========================
     * POSTGRESQL
     * =========================
     */

    public String getDatabaseHost() {
        return databaseHost;
    }

    public int getDatabasePort() {
        return databasePort;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public int getDatabaseMaximumPoolSize() {
        return databaseMaximumPoolSize;
    }

    public int getDatabaseMinimumIdle() {
        return databaseMinimumIdle;
    }

    public long getDatabaseConnectionTimeout() {
        return databaseConnectionTimeout;
    }

    public long getDatabaseIdleTimeout() {
        return databaseIdleTimeout;
    }

    public long getDatabaseMaxLifetime() {
        return databaseMaxLifetime;
    }

    /*
     * =========================
     * DEFAULT CONFIG
     * =========================
     */

    public static VelocityConfig defaultConfig() {

        return new VelocityConfig(
                /*
                 * Redis
                 */
                "localhost",
                6379,

                /*
                 * Proxy
                 */
                "PROXY-01",

                /*
                 * PostgreSQL
                 */
                "127.0.0.1",
                5432,
                "laboon_web",
                "laboon",
                "laboon_dev_2026",

                /*
                 * Pool
                 */
                10,
                2,
                5000,
                600000,
                1800000
        );
    }
}