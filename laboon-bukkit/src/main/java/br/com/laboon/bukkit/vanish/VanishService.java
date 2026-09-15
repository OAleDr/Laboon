package br.com.laboon.bukkit.vanish;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.PlayerVanishMessage;
import br.com.laboon.core.messaging.PlayerVanishSerializer;
import br.com.laboon.core.vanish.GlobalVanishRepository;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VanishService {

    private final JavaPlugin plugin;
    private final MessageBus messageBus;
    private final GlobalVanishRepository repository;
    private final AccountManager accountManager;

    private final Set<UUID> vanishedPlayers =
            ConcurrentHashMap.newKeySet();

    public VanishService(
            JavaPlugin plugin,
            MessageBus messageBus,
            GlobalVanishRepository repository,
            AccountManager accountManager
    ) {
        this.plugin = plugin;
        this.messageBus = messageBus;
        this.repository = repository;
        this.accountManager = accountManager;
    }

    public void register() {
        syncFromRedis();

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

                    Bukkit.getScheduler().runTask(
                            plugin,
                            () -> applyVisibilityForAll()
                    );
                }
        );

        Bukkit.getOnlinePlayers().forEach(this::apply);
    }

    public void enable(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();

        vanishedPlayers.add(uuid);
        repository.setVanished(uuid);

        publish(
                new PlayerVanishMessage(uuid, true)
        );

        apply(player);
        applyVisibilityForAll();
    }

    public void disable(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();

        vanishedPlayers.remove(uuid);
        repository.clear(uuid);

        publish(
                new PlayerVanishMessage(uuid, false)
        );

        applyVisibilityForAll();
    }

    public void toggle(Player player) {
        if (player == null) {
            return;
        }

        if (isVanished(player.getUniqueId())) {
            disable(player);
        } else {
            enable(player);
        }
    }

    public boolean isVanished(UUID uuid) {
        return uuid != null && vanishedPlayers.contains(uuid);
    }

    public boolean isVanished(Player player) {
        return player != null && isVanished(player.getUniqueId());
    }

    public List<Player> getVanishedPlayers() {
        List<Player> result = new ArrayList<>();

        for (UUID uuid : vanishedPlayers) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                result.add(player);
            }
        }

        return result;
    }

    public List<Player> getNonVanishedPlayers() {
        return getNonVanishedPlayers(Bukkit.getOnlinePlayers());
    }

    public List<Player> getActivePlayers() {
        return getNonVanishedPlayers();
    }

    public List<Player> getNonVanishedPlayers(
            Collection<? extends Player> players
    ) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }

        List<Player> result = new ArrayList<>();

        for (Player player : players) {
            if (player == null) {
                continue;
            }

            if (!isVanished(player.getUniqueId())) {
                result.add(player);
            }
        }

        return result;
    }

    public int getActivePlayerCount() {
        return getNonVanishedPlayers().size();
    }

    public boolean isVisibleTo(
            Player viewer,
            Player target
    ) {
        if (viewer == null || target == null) {
            return false;
        }

        if (!isVanished(target)) {
            return true;
        }

        if (!isStaff(viewer)) {
            return false;
        }

        if (!isStaff(target)) {
            return false;
        }

        Group viewerGroup = getGroup(viewer);
        Group targetGroup = getGroup(target);

        if (viewerGroup == null || targetGroup == null) {
            return false;
        }

        return viewerGroup.getPower() >= targetGroup.getPower();
    }

    public void hideFrom(
            Player viewer,
            Player target
    ) {
        if (viewer == null || target == null || viewer.equals(target)) {
            return;
        }

        if (!isVisibleTo(viewer, target)) {
            viewer.hidePlayer(plugin, target);
        } else {
            viewer.showPlayer(plugin, target);
        }
    }

    public void showTo(
            Player viewer,
            Player target
    ) {
        if (viewer == null || target == null || viewer.equals(target)) {
            return;
        }

        if (isVisibleTo(viewer, target)) {
            viewer.showPlayer(plugin, target);
        } else {
            viewer.hidePlayer(plugin, target);
        }
    }

    public void apply(Player target) {
        if (target == null) {
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }

            hideFrom(viewer, target);
        }
    }

    public void applyVisibilityForAll() {
        for (Player target : Bukkit.getOnlinePlayers()) {
            apply(target);
        }
    }

    /**
     * Deve ser usado pelos minigames para contar somente
     * jogadores participantes/ativos.
     */
    public boolean isActive(Player player) {
        return player != null && !isVanished(player.getUniqueId());
    }

    /**
     * Não chama disable() aqui.
     *
     * O estado global precisa sobreviver à troca de servidor.
     */
    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        vanishedPlayers.remove(player.getUniqueId());
    }

    private void syncFromRedis() {
        vanishedPlayers.clear();

        for (String rawKey : repository.getVanishedPlayersRaw()) {
            UUID uuid = repository.parseUuid(rawKey);

            if (uuid != null) {
                vanishedPlayers.add(uuid);
            }
        }
    }

    private boolean isStaff(Player player) {
        Group group = getGroup(player);
        return group != null && group.getPower() > Group.DEFAULT.getPower();
    }

    private Group getGroup(Player player) {
        Account account = accountManager.get(player.getUniqueId());

        if (account == null) {
            return Group.DEFAULT;
        }

        return account.getGroup();
    }

    private void publish(PlayerVanishMessage message) {
        messageBus.publish(
                Channels.PLAYER_VANISH,
                PlayerVanishSerializer.serialize(message)
        );
    }
}
