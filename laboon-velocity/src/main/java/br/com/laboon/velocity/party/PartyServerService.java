package br.com.laboon.velocity.party;

import br.com.laboon.core.party.Party;
import br.com.laboon.core.party.PartyManager;
import br.com.laboon.core.party.PartyMember;
import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerRegistry;
import br.com.laboon.core.server.ServerRole;
import br.com.laboon.core.server.ServerState;
import br.com.laboon.core.server.ServerType;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PartyServerService {

    private final ProxyServer proxyServer;
    private final PartyManager partyManager;
    private final ServerRegistry serverRegistry;

    public PartyServerService(ProxyServer proxyServer, PartyManager partyManager, ServerRegistry serverRegistry) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (partyManager == null) {
            throw new IllegalArgumentException("PartyManager não pode ser nulo.");
        }

        if (serverRegistry == null) {
            throw new IllegalArgumentException("ServerRegistry não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.partyManager = partyManager;
        this.serverRegistry = serverRegistry;
    }

    /*
     * =========================
     * ENTRAR COM PARTY
     * =========================
     */

    public boolean sendPartyTo(UUID leader, ServerType serverType) {

        if (leader == null) {
            return false;
        }

        if (serverType == null) {
            return false;
        }

        Party party = partyManager.getByMember(leader);

        /*
         * Se não estiver em Party,
         * o jogador pode entrar sozinho.
         */
        if (party == null) {

            Player player = getPlayer(leader);

            if (player == null) {
                return false;
            }

            ServerInfo server = findAvailableServer(serverType);

            if (server == null) {

                send(player, "§cNão existe um servidor disponível.");

                return false;
            }

            connect(player, server);

            return true;
        }

        /*
         * Somente o líder pode levar
         * a Party para outro servidor.
         */
        if (!party.isLeader(leader)) {

            Player player = getPlayer(leader);

            if (player != null) {

                send(player, "§cSomente o líder pode iniciar a partida da Party.");
            }

            return false;
        }

        /*
         * Procura um servidor disponível.
         */
        ServerInfo server = findAvailableServer(serverType);

        if (server == null) {

            notifyParty(party, "§cNão existe um servidor disponível no momento.");

            return false;
        }

        /*
         * Conecta todos os membros.
         */
        List<Player> connectedPlayers = new ArrayList<>();

        for (PartyMember member : party.getMembers()) {

            Player player = getPlayer(member.getUniqueId());

            if (player == null) {
                continue;
            }

            connectedPlayers.add(player);
        }

        if (connectedPlayers.isEmpty()) {

            return false;
        }

        /*
         * Informa a Party.
         */
        notifyParty(party, "§aSua Party está entrando em §f" + server.getName() + "§a!");

        /*
         * Conecta todos os membros online.
         */
        for (Player player : connectedPlayers) {

            connect(player, server);
        }

        return true;
    }

    /*
     * =========================
     * SERVIDOR
     * =========================
     */

    private ServerInfo findAvailableServer(ServerType type) {

        /*
         * Primeiro tenta encontrar um servidor
         * WAITING, que é o estado ideal para
         * receber uma Party.
         */
        List<ServerInfo> waiting = serverRegistry.findByType(type).stream().filter(server -> server.getState() == ServerState.WAITING).filter(server -> server.getPlayers() < server.getMaxPlayers()).sorted((first, second) -> Integer.compare(first.getPlayers(), second.getPlayers())).toList();

        if (!waiting.isEmpty()) {

            return waiting.get(0);
        }

        /*
         * Caso não exista WAITING,
         * permite ONLINE.
         */
        return serverRegistry.findByType(type).stream().filter(server -> server.getState() == ServerState.ONLINE).filter(server -> server.getPlayers() < server.getMaxPlayers()).sorted((first, second) -> Integer.compare(first.getPlayers(), second.getPlayers())).findFirst().orElse(null);
    }

    /*
     * =========================
     * CONNECTION
     * =========================
     */

    private void connect(Player player, ServerInfo server) {

        RegisteredServer registeredServer = proxyServer.getAllServers().stream().filter(candidate -> candidate.getServerInfo().getName().equalsIgnoreCase(server.getName())).findFirst().orElse(null);

        if (registeredServer == null) {

            send(player, "§cO servidor §f" + server.getName() + " §cnão está registrado no Velocity.");

            return;
        }

        player.createConnectionRequest(registeredServer).connect();
    }

    /*
     * =========================
     * PLAYER
     * =========================
     */

    private Player getPlayer(UUID uniqueId) {

        return proxyServer.getPlayer(uniqueId).orElse(null);
    }

    /*
     * =========================
     * PARTY MESSAGE
     * =========================
     */

    private void notifyParty(Party party, String message) {

        for (PartyMember member : party.getMembers()) {

            Player player = getPlayer(member.getUniqueId());

            if (player == null) {
                continue;
            }

            send(player, message);
        }
    }

    private void send(Player player, String message) {

        player.sendMessage(Component.text(message));
    }
}