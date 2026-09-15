package br.com.laboon.velocity.vanish;

import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.PlayerVanishMessage;
import br.com.laboon.core.messaging.PlayerVanishSerializer;
import br.com.laboon.core.vanish.GlobalVanishRepository;

import com.velocitypowered.api.proxy.Player;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VelocityVanishService {

    private final MessageBus messageBus;
    private final GlobalVanishRepository repository;

    private final Set<UUID> vanishedPlayers =
            ConcurrentHashMap.newKeySet();

    public VelocityVanishService(
            MessageBus messageBus,
            GlobalVanishRepository repository
    ) {
        this.messageBus = messageBus;
        this.repository = repository;
    }

    public void start() {
        sync();

        messageBus.subscribe(
                Channels.PLAYER_VANISH,
                (channel, rawMessage) -> {
                    PlayerVanishMessage message =
                            PlayerVanishSerializer.deserialize(rawMessage);

                    if (message == null) {
                        return;
                    }

                    if (message.vanished()) {
                        vanishedPlayers.add(message.playerUuid());
                    } else {
                        vanishedPlayers.remove(message.playerUuid());
                    }
                }
        );
    }

    public boolean isVanished(UUID uuid) {
        return uuid != null && vanishedPlayers.contains(uuid);
    }

    public int getPublicPlayerCount(Collection<Player> players) {
        if (players == null || players.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (Player player : players) {
            if (player != null && !isVanished(player.getUniqueId())) {
                count++;
            }
        }

        return count;
    }

    public void handleNetworkDisconnect(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!isVanished(uuid)) {
            vanishedPlayers.remove(uuid);
            return;
        }

        vanishedPlayers.remove(uuid);
        repository.clear(uuid);

        messageBus.publish(
                Channels.PLAYER_VANISH,
                PlayerVanishSerializer.serialize(
                        new PlayerVanishMessage(uuid, false)
                )
        );
    }

    public void sync() {
        vanishedPlayers.clear();

        for (String rawKey : repository.getVanishedPlayersRaw()) {
            UUID uuid = repository.parseUuid(rawKey);

            if (uuid != null) {
                vanishedPlayers.add(uuid);
            }
        }
    }
}
