package br.com.laboon.bukkit.messaging;

import br.com.laboon.bukkit.LaboonBukkit;
import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.PlayerActionMessage;
import br.com.laboon.core.messaging.PlayerActionSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * API Bukkit para ações entre jogadores/servidores.
 *
 * O método público principal para TP é:
 *
 * teleport(Player staff, Player target)
 *
 * O Velocity fica responsável por descobrir em qual servidor
 * o target está e mover o staff para esse servidor.
 *
 * O teleport final acontece aqui, no Bukkit, quando ambos
 * estiverem carregados no mesmo servidor.
 *
 * O ponto de vanish fica propositalmente fora desta implementação.
 */
public final class BukkitPlayerActionService {

    private final LaboonBukkit plugin;
    private final MessageBus messageBus;

    private final ConcurrentMap<UUID, PendingTeleport> pendingTeleports =
            new ConcurrentHashMap<>();

    public BukkitPlayerActionService(
            LaboonBukkit plugin,
            MessageBus messageBus
    ) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }

        if (messageBus == null) {
            throw new IllegalArgumentException("MessageBus não pode ser nulo.");
        }

        this.plugin = plugin;
        this.messageBus = messageBus;
    }

    /**
     * Registra o listener do canal PLAYER_ACTION.
     */
    public void register() {
        messageBus.subscribe(
                Channels.PLAYER_ACTION,
                (channel, rawMessage) -> handle(rawMessage)
        );
    }

    /**
     * Solicita conexão para um servidor.
     */
    public void connect(
            Player player,
            String server
    ) {
        if (player == null || server == null || server.isBlank()) {
            return;
        }

        publish(
                PlayerActionMessage.connect(
                        player.getUniqueId(),
                        server
                )
        );
    }

    /**
     * Solicita teleport de um jogador para outro.
     *
     * Exemplo:
     *
     * playerActionService.teleport(staff, target);
     */

    public void teleport(
            Player staff,
            Player target
    ) {
        teleport(staff, target.getUniqueId());
    }

    public void teleport(
            Player staff,
            UUID target
    ) {
        if (staff == null || target == null) {
            return;
        }

        if (staff.getUniqueId().equals(target)) {
            return;
        }

        pendingTeleports.put(
                staff.getUniqueId(),
                new PendingTeleport(
                        staff.getUniqueId(),
                        target
                )
        );

        publish(
                PlayerActionMessage.teleport(
                        staff.getUniqueId(),
                        target
                )
        );
    }

    private void handle(String rawMessage) {
        PlayerActionMessage message =
                PlayerActionSerializer.deserialize(rawMessage);

        if (message == null) {
            return;
        }

        if (message.getAction() != PlayerActionMessage.Action.TELEPORT) {
            return;
        }

        if (message.getStatus() == null
                || message.getStatus() == PlayerActionMessage.Status.REQUEST) {
            return;
        }

        Player staff = Bukkit.getPlayer(message.getPlayerUuid());

        if (staff == null) {
            pendingTeleports.remove(message.getPlayerUuid());
            return;
        }

        switch (message.getStatus()) {
            case SUCCESS:
            case SAME_SERVER:
                handleTeleportAccepted(message);
                break;

            case FAILED:
            case NOT_FOUND:
                pendingTeleports.remove(message.getPlayerUuid());
                staff.sendMessage("§cNão foi possível realizar o teleport.");
                break;

            default:
                break;
        }
    }

    private void handleTeleportAccepted(PlayerActionMessage message) {
        UUID staffUuid = message.getPlayerUuid();
        UUID targetUuid = message.getTargetUuid();

        if (targetUuid == null) {
            pendingTeleports.remove(staffUuid);
            return;
        }

        PendingTeleport pending = pendingTeleports.get(staffUuid);

        if (pending == null
                || !pending.targetUuid().equals(targetUuid)) {
            return;
        }

        Bukkit.getScheduler().runTask(
                plugin,
                () -> waitForPlayers(
                        staffUuid,
                        targetUuid
                )
        );
    }

    private void waitForPlayers(
            UUID staffUuid,
            UUID targetUuid
    ) {
        PendingTeleport pending = pendingTeleports.get(staffUuid);

        if (pending == null) {
            return;
        }

        Player staff = Bukkit.getPlayer(staffUuid);
        Player target = Bukkit.getPlayer(targetUuid);

        if (staff == null || target == null) {
            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    () -> waitForPlayers(staffUuid, targetUuid),
                    2L
            );
            return;
        }

        if (!staff.isOnline() || !target.isOnline()) {
            pendingTeleports.remove(staffUuid);
            return;
        }

        if (!staff.getWorld().getUID().equals(target.getWorld().getUID())) {
            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    () -> waitForPlayers(staffUuid, targetUuid),
                    2L
            );
            return;
        }

        Location location = target.getLocation().clone();

        /*
         * Futuramente, antes deste teleport, o fluxo poderá chamar:
         *
         * vanishService.hide(staff);
         *
         * O Vanish permanece exclusivamente no Bukkit.
         */
        staff.teleport(location);

        pendingTeleports.remove(staffUuid);
    }

    private void publish(PlayerActionMessage message) {
        messageBus.publish(
                Channels.PLAYER_ACTION,
                PlayerActionSerializer.serialize(message)
        );
    }

    private record PendingTeleport(
            UUID staffUuid,
            UUID targetUuid
    ) {
    }
}
