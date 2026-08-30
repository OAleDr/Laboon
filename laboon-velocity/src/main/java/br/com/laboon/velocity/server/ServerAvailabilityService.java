package br.com.laboon.velocity.server;

import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerState;
import br.com.laboon.core.server.ServerType;

public final class ServerAvailabilityService {

    public boolean isAvailable(
            ServerInfo server
    ) {

        if (server == null) {
            return false;
        }

        if (server.getPlayers() >= server.getMaxPlayers()) {
            return false;
        }

        return switch (server.getRole()) {

            case LOBBY ->
                    server.getState() == ServerState.ONLINE
                            || server.getState() == ServerState.WAITING;

            case GAME ->
                    server.getState() == ServerState.WAITING;

            case PROXY ->
                    false;
        };
    }

    public String getStatus(
            ServerInfo server
    ) {

        if (server.getPlayers() >= server.getMaxPlayers()) {
            return "§c[CHEIO]";
        }

        return switch (server.getState()) {

            case STARTING ->
                    "§e[INICIANDO]";

            case WAITING ->
                    "§a[DISPONÍVEL]";

            case INGAME ->
                    "§b[EM JOGO]";

            case ENDING ->
                    "§6[FINALIZANDO]";

            case ONLINE ->
                    "§a[ONLINE]";

            case OFFLINE ->
                    "§c[OFFLINE]";
        };
    }

    public String getTypeName(
            ServerType type
    ) {

        return switch (type) {

            case NETWORK ->
                    "LOBBY";

            case BEDWARS ->
                    "BEDWARS";

            case SKYWARS ->
                    "SKYWARS";

            case HARDCORE_GAMES ->
                    "HARDCORE GAMES";

            case KITPVP ->
                    "KITPVP";
        };
    }

}