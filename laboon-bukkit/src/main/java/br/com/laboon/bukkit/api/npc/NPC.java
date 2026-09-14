package br.com.laboon.bukkit.api.npc;

import br.com.laboon.bukkit.LaboonBukkit;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class NPC {

    private static final Map<Integer, NPC> NPCS =
            new ConcurrentHashMap<>();

    private static final AtomicInteger ENTITY_ID =
            new AtomicInteger(1_000_000);

    private final JavaPlugin plugin;

    private final int entityId;

    private final UUID uniqueId;

    private final Profile npcProfile;

    private final WrappedGameProfile profile;

    private Location location;

    private boolean hideName;

    private NPCClickAction clickAction;

    public NPC(
            Location location,
            String name
    ) {
        this(
                location,
                new Profile(name)
        );
    }

    public NPC(
            Location location,
            Profile profile
    ) {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Profile não pode ser nulo."
            );
        }

        this.plugin = LaboonBukkit.getInstance();

        this.location =
                location == null
                        ? null
                        : location.clone();

        this.npcProfile = profile;

        this.profile = profile.asWrapped();

        this.entityId =
                ENTITY_ID.getAndIncrement();

        this.uniqueId =
                this.npcProfile.getUniqueId();

        if (this.uniqueId == null) {
            throw new IllegalArgumentException(
                    "WrappedGameProfile precisa possuir um UUID."
            );
        }

        NPCS.put(
                this.entityId,
                this
        );
    }

    public NPC(
            JavaPlugin plugin,
            Location location,
            WrappedGameProfile profile
    ) {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Profile não pode ser nulo."
            );
        }

        UUID profileId =
                profile.getUUID();

        if (profileId == null) {
            throw new IllegalArgumentException(
                    "WrappedGameProfile precisa possuir um UUID."
            );
        }

        this.plugin = plugin;

        this.location =
                location == null
                        ? null
                        : location.clone();

        this.profile = profile;

        this.npcProfile = null;

        this.entityId =
                ENTITY_ID.getAndIncrement();

        this.uniqueId = profileId;

        NPCS.put(
                this.entityId,
                this
        );
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public int getEntityId() {
        return entityId;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public WrappedGameProfile getProfile() {
        return profile;
    }

    public Profile getNpcProfile() {
        return npcProfile;
    }

    public WrappedSignedProperty getSkinProperty() {

        if (npcProfile == null) {
            return null;
        }

        return npcProfile.getSignedProperty();
    }

    public Location getLocation() {

        return location == null
                ? null
                : location.clone();
    }

    public void setLocation(Location location) {

        if (location != null) {
            this.location = location.clone();
        }
    }

    public void teleport(Location location) {

        if (location == null) {
            return;
        }

        this.location =
                location.clone();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (this.location.getWorld() == null
                    || !this.location.getWorld()
                    .equals(player.getWorld())) {

                hide(player);

                continue;
            }

            double distanceSquared =
                    player.getLocation()
                            .distanceSquared(
                                    this.location
                            );

            if (distanceSquared
                    <= NPCListener.getViewDistanceSquared()) {

                show(player);

            } else {

                hide(player);
            }
        }
    }

    public boolean isHideName() {
        return hideName;
    }

    public NPC setHideName(boolean hideName) {

        this.hideName = hideName;

        return this;
    }

    public NPCClickAction getClickAction() {
        return clickAction;
    }

    public NPC setClickAction(
            NPCClickAction clickAction
    ) {

        this.clickAction = clickAction;

        return this;
    }

    public boolean isTouchable() {
        return clickAction != null;
    }

    public void show(Player player) {

        if (player == null
                || !player.isOnline()
                || location == null
                || location.getWorld() == null) {

            return;
        }

        if (!location.getWorld()
                .equals(player.getWorld())) {

            return;
        }

        NPCListener.sendSpawn(
                this,
                player
        );
    }

    public void hide(Player player) {

        if (player == null
                || !player.isOnline()) {

            return;
        }

        NPCListener.sendDestroy(
                this,
                player
        );
    }

    public void remove() {

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            hide(player);
        }

        NPCS.remove(entityId);
    }

    public static NPC getNPC(int entityId) {
        return NPCS.get(entityId);
    }

    public static boolean isNPC(int entityId) {
        return NPCS.containsKey(entityId);
    }

    public static void removeNPC(int entityId) {

        NPC npc =
                NPCS.remove(entityId);

        if (npc == null) {
            return;
        }

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            npc.hide(player);
        }
    }

    public static Map<Integer, NPC> getNPCs() {
        return Map.copyOf(NPCS);
    }
}