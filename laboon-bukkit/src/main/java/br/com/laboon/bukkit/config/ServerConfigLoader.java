package br.com.laboon.bukkit.config;

import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerType;

import org.bukkit.plugin.java.JavaPlugin;

public final class ServerConfigLoader {

    private ServerConfigLoader() {
    }

    public static ServerConfig load(
            JavaPlugin plugin
    ) {

        String name =
                plugin.getConfig()
                        .getString("server.name");

        String type =
                plugin.getConfig()
                        .getString("server.type");

        String role =
                plugin.getConfig()
                        .getString("server.role");

        String host =
                plugin.getConfig()
                        .getString("server.host");

        int port =
                plugin.getConfig()
                        .getInt("server.port");

        int maxPlayers =
                plugin.getConfig()
                        .getInt("server.max-players");

        validate(
                name,
                type,
                role,
                host,
                port,
                maxPlayers
        );

        ServerType serverType =
                parseType(type);

        ServerRole serverRole =
                parseRole(role);

        return new ServerConfig(
                name,
                serverType,
                serverRole,
                host,
                port,
                maxPlayers
        );
    }

    private static void validate(
            String name,
            String type,
            String role,
            String host,
            int port,
            int maxPlayers
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalStateException(
                    "server.name não configurado."
            );
        }

        if (type == null || type.isBlank()) {
            throw new IllegalStateException(
                    "server.type não configurado."
            );
        }

        if (role == null || role.isBlank()) {
            throw new IllegalStateException(
                    "server.role não configurado."
            );
        }

        if (host == null || host.isBlank()) {
            throw new IllegalStateException(
                    "server.host não configurado."
            );
        }

        if (port <= 0 || port > 65535) {
            throw new IllegalStateException(
                    "server.port inválida: "
                            + port
            );
        }

        if (maxPlayers <= 0) {
            throw new IllegalStateException(
                    "server.max-players deve ser maior que zero."
            );
        }
    }

    private static ServerType parseType(
            String value
    ) {

        try {

            return ServerType.valueOf(
                    value.toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "server.type inválido: "
                            + value,
                    exception
            );
        }
    }

    private static ServerRole parseRole(
            String value
    ) {

        try {

            return ServerRole.valueOf(
                    value.toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "server.role inválido: "
                            + value,
                    exception
            );
        }
    }
}