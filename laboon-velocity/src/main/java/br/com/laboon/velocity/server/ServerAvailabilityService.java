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

        if (server.getPlayers()
                >= server.getMaxPlayers()) {

            return "server.status.full";
        }

        return switch (server.getState()) {

            case STARTING ->
                    "server.status.starting";

            case WAITING ->
                    "server.status.waiting";

            case INGAME ->
                    "server.status.ingame";

            case ENDING ->
                    "server.status.ending";

            case ONLINE ->
                    "server.status.online";

            case OFFLINE ->
                    "server.status.offline";
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