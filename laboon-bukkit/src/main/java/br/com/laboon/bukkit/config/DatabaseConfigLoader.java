package br.com.laboon.bukkit.config;

import br.com.laboon.core.database.DatabaseConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class DatabaseConfigLoader {

    private DatabaseConfigLoader() {
    }

    public static DatabaseConfig load(
            JavaPlugin plugin
    ) {

        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection(
                        "database"
                );

        if (section == null) {
            throw new IllegalStateException(
                    "Seção 'database' não encontrada no config.yml."
            );
        }

        ConfigurationSection pool =
                section.getConfigurationSection("pool");

        if (pool == null) {
            throw new IllegalStateException(
                    "Seção 'database.pool' não encontrada."
            );
        }

        return new DatabaseConfig(
                section.getString(
                        "host",
                        "127.0.0.1"
                ),

                section.getInt(
                        "port",
                        5432
                ),

                section.getString(
                        "database",
                        "laboon"
                ),

                section.getString(
                        "username",
                        "laboon"
                ),

                section.getString(
                        "password",
                        ""
                ),

                pool.getInt(
                        "maximum-size",
                        10
                ),

                pool.getInt(
                        "minimum-idle",
                        2
                ),

                pool.getLong(
                        "connection-timeout",
                        5000
                ),

                pool.getLong(
                        "idle-timeout",
                        600000
                ),

                pool.getLong(
                        "max-lifetime",
                        1800000
                )
        );
    }
}