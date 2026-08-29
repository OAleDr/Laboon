package br.com.laboon.velocity.config;

public final class VelocityConfig {

    private final String redisHost;
    private final int redisPort;
    private final String proxyName;

    public VelocityConfig(
            String redisHost,
            int redisPort,
            String proxyName
    ) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.proxyName = proxyName;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getProxyName() {
        return proxyName;
    }

    public static VelocityConfig defaultConfig() {
        return new VelocityConfig(
                "localhost",
                6379,
                "PROXY-01"
        );
    }
}