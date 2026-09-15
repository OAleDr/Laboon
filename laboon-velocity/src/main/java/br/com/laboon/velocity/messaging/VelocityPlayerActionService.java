package br.com.laboon.velocity.messaging;

import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.PlayerActionMessage;
import br.com.laboon.core.messaging.PlayerActionSerializer;
import br.com.laboon.core.server.ServerRegistry;
import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.velocity.player.PlayerManager;
import br.com.laboon.velocity.server.ServerConnectionService;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

/**
 * Processa PlayerActionMessage no Velocity.
 *
 * O Velocity é responsável apenas pela parte de Network:
 * localizar jogadores, descobrir servidor e conectar.
 *
 * O teleport Bukkit continua sendo responsabilidade do Bukkit.
 */
public final class VelocityPlayerActionService {

    private final ProxyServer proxyServer;
    private final MessageBus messageBus;
    private final PlayerManager playerManager;
    private final AccountManager accountManager;
    private final ServerRegistry serverRegistry;
    private final ServerConnectionService connectionService;

    public VelocityPlayerActionService(
            ProxyServer proxyServer,
            MessageBus messageBus,
            PlayerManager playerManager,
            AccountManager accountManager,
            ServerRegistry serverRegistry,
            ServerConnectionService connectionService
    ) {
        this.proxyServer = proxyServer;
        this.messageBus = messageBus;
        this.playerManager = playerManager;
        this.accountManager = accountManager;
        this.serverRegistry = serverRegistry;
        this.connectionService = connectionService;
    }

    public void register() {
        messageBus.subscribe(
                Channels.PLAYER_ACTION,
                (channel, rawMessage) -> handle(rawMessage)
        );
    }

    private void handle(String rawMessage) {
        PlayerActionMessage message =
                PlayerActionSerializer.deserialize(rawMessage);

        if (message == null) {
            return;
        }

        if (message.getStatus() != PlayerActionMessage.Status.REQUEST) {
            return;
        }

        switch (message.getAction()) {
            case CONNECT:
                handleConnect(message);
                break;

            case TELEPORT:
                handleTeleport(message);
                break;
        }
    }

    private void handleConnect(PlayerActionMessage message) {
        Player player = playerManager.find(message.getPlayerUuid());

        if (player == null || message.getServer() == null || message.getServer().isBlank()) {
            publishResult(
                    PlayerActionMessage.result(
                            PlayerActionMessage.Action.CONNECT,
                            message.getPlayerUuid(),
                            null,
                            message.getServer(),
                            PlayerActionMessage.Status.FAILED
                    )
            );
            return;
        }

        ServerInfo server = serverRegistry.find(message.getServer());
        RegisteredServer registeredServer = findRegisteredServer(message.getServer());

        if (server == null || registeredServer == null) {
            publishResult(
                    PlayerActionMessage.result(
                            PlayerActionMessage.Action.CONNECT,
                            message.getPlayerUuid(),
                            null,
                            message.getServer(),
                            PlayerActionMessage.Status.NOT_FOUND
                    )
            );
            return;
        }

        connectionService.connect(
                player,
                server,
                () -> publishResult(
                        PlayerActionMessage.result(
                                PlayerActionMessage.Action.CONNECT,
                                message.getPlayerUuid(),
                                null,
                                message.getServer(),
                                PlayerActionMessage.Status.FAILED
                        )
                )
        );
    }

    private void handleTeleport(PlayerActionMessage message) {
        Player staff = playerManager.find(message.getPlayerUuid());

        if (staff == null || message.getTargetUuid() == null) {
            publishResult(
                    PlayerActionMessage.result(
                            PlayerActionMessage.Action.TELEPORT,
                            message.getPlayerUuid(),
                            message.getTargetUuid(),
                            null,
                            PlayerActionMessage.Status.NOT_FOUND
                    )
            );
            return;
        }

        Player target = playerManager.find(message.getTargetUuid());

        if (target == null) {
            publishResult(
                    PlayerActionMessage.result(
                            PlayerActionMessage.Action.TELEPORT,
                            message.getPlayerUuid(),
                            message.getTargetUuid(),
                            null,
                            PlayerActionMessage.Status.NOT_FOUND
                    )
            );
            return;
        }

        String staffServer = currentServer(staff);
        String targetServer = currentServer(target);

        if (targetServer == null) {
            publishResult(
                    PlayerActionMessage.result(
                            PlayerActionMessage.Action.TELEPORT,
                            staff.getUniqueId(),
                            target.getUniqueId(),
                            null,
                            PlayerActionMessage.Status.NOT_FOUND
                    )
            );
            return;
        }

        if (targetServer.equalsIgnoreCase(staffServer)) {
            publishResult(
                    PlayerActionMessage.result(
                            PlayerActionMessage.Action.TELEPORT,
                            staff.getUniqueId(),
                            target.getUniqueId(),
                            targetServer,
                            PlayerActionMessage.Status.SAME_SERVER
                    )
            );
            return;
        }

        ServerInfo targetServerInfo = serverRegistry.find(targetServer);
        RegisteredServer registeredServer = findRegisteredServer(targetServer);

        if (targetServerInfo == null || registeredServer == null) {
            publishResult(
                    PlayerActionMessage.result(
                            PlayerActionMessage.Action.TELEPORT,
                            staff.getUniqueId(),
                            target.getUniqueId(),
                            targetServer,
                            PlayerActionMessage.Status.NOT_FOUND
                    )
            );
            return;
        }

        connectionService.connect(
                staff,
                targetServerInfo,
                () -> publishResult(
                        PlayerActionMessage.result(
                                PlayerActionMessage.Action.TELEPORT,
                                staff.getUniqueId(),
                                target.getUniqueId(),
                                targetServer,
                                PlayerActionMessage.Status.FAILED
                        )
                )
        );

        /*
         * O Bukkit recebe a confirmação para começar a aguardar
         * ambos os jogadores no mesmo servidor.
         */
        publishResult(
                PlayerActionMessage.result(
                        PlayerActionMessage.Action.TELEPORT,
                        staff.getUniqueId(),
                        target.getUniqueId(),
                        targetServer,
                        PlayerActionMessage.Status.SUCCESS
                )
        );
    }

    private String currentServer(Player player) {
        return player.getCurrentServer()
                .map(connection ->
                        connection.getServer()
                                .getServerInfo()
                                .getName()
                )
                .orElse(null);
    }

    private RegisteredServer findRegisteredServer(String serverName) {
        if (serverName == null || serverName.isBlank()) {
            return null;
        }

        return proxyServer.getServer(serverName).orElse(null);
    }

    private void publishResult(PlayerActionMessage message) {
        messageBus.publish(
                Channels.PLAYER_ACTION,
                PlayerActionSerializer.serialize(message)
        );
    }
}
