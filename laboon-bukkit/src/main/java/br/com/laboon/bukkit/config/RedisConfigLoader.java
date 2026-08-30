package br.com.laboon.bukkit.config;

import org.bukkit.plugin.java.JavaPlugin;

public final class RedisConfigLoader {

    private RedisConfigLoader() {
    }

    public static RedisConfig load(
            JavaPlugin plugin
    ) {

        String host =
                plugin.getConfig()
                        .getString("redis.host");

        int port =
                plugin.getConfig()
                        .getInt("redis.port");

        if (host == null || host.isBlank()) {
            throw new IllegalStateException(
                    "redis.host não configurado."
            );
        }

        if (port <= 0 || port > 65535) {
            throw new IllegalStateException(
                    "redis.port inválida: "
                            + port
            );
        }

        return new RedisConfig(
                host,
                port
        );
    }
}