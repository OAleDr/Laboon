package br.com.laboon.bukkit.api;

import br.com.laboon.bukkit.api.hologram.HologramClickAction;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public final class Hologram {

    private static final double LINE_DISTANCE = 0.285D;
    private static final double TEXT_HEIGHT = 1.7D;
    private static final double TOUCH_HEIGHT = 0.2D;

    private static final Map<Integer, Hologram> HOLOGRAMS = new java.util.concurrent.ConcurrentHashMap<>();

    private static final AtomicInteger NEXT_ID = new AtomicInteger(2_000_000);

    private final ProtocolManager protocolManager;

    private final int touchId;
    private final UUID touchUuid;

    private final Set<UUID> viewers = new CopyOnWriteArraySet<>();

    private final List<HologramLine> lines = new ArrayList<>();

    private Location location;

    private HologramClickAction clickAction;

    public Hologram(Location location, String text) {

        if (location == null) {
            throw new IllegalArgumentException("Location não pode ser nula.");
        }

        if (text == null) {
            throw new IllegalArgumentException("Texto não pode ser nulo.");
        }

        this.protocolManager = ProtocolLibrary.getProtocolManager();

        this.location = location.clone();

        this.touchId = nextId();

        this.touchUuid = UUID.randomUUID();

        setLines(text);

        HOLOGRAMS.put(touchId, this);
    }

    public Hologram(Location location) {
        this(location, "");
    }

    private static int nextId() {
        return NEXT_ID.getAndIncrement();
    }

    /*
     * ==================================================
     * GETTERS
     * ==================================================
     */

    public Location getLocation() {
        return location.clone();
    }

    public String getText() {

        return String.join("\n", getLines());
    }

    public List<String> getLines() {

        List<String> result = new ArrayList<>();

        for (HologramLine line : lines) {
            result.add(line.text());
        }

        return List.copyOf(result);
    }

    public int getTouchId() {
        return touchId;
    }

    public HologramClickAction getClickAction() {
        return clickAction;
    }

    public boolean isTouchable() {
        return clickAction != null;
    }

    public boolean isShownFor(Player player) {

        if (player == null) {
            return false;
        }

        return viewers.contains(player.getUniqueId());
    }

    /*
     * ==================================================
     * TEXT
     * ==================================================
     */

    public Hologram setText(String text) {

        if (text == null) {
            throw new IllegalArgumentException("Texto não pode ser nulo.");
        }

        setLines(text);

        return this;
    }

    public Hologram addLine(String line) {

        if (line == null) {
            return this;
        }

        lines.add(new HologramLine(nextId(), UUID.randomUUID(), line));

        refresh();

        return this;
    }

    public Hologram setLines(String... lines) {

        this.lines.clear();

        if (lines != null) {

            for (String line : lines) {

                if (line == null) {
                    continue;
                }

                this.lines.add(new HologramLine(nextId(), UUID.randomUUID(), line));
            }
        }

        refresh();

        return this;
    }

    public Hologram clearLines() {

        lines.clear();

        refresh();

        return this;
    }

    /*
     * ==================================================
     * ACTION
     * ==================================================
     */

    public Hologram setClickAction(HologramClickAction clickAction) {

        this.clickAction = clickAction;

        refresh();

        return this;
    }

    public Hologram clearClickAction() {

        this.clickAction = null;

        refresh();

        return this;
    }

    /*
     * ==================================================
     * SHOW
     * ==================================================
     */

    public void show(Player player) {

        if (player == null || !player.isOnline()) {

            return;
        }

        if (location.getWorld() == null) {
            return;
        }

        if (!location.getWorld().equals(player.getWorld())) {

            return;
        }

        if (!viewers.add(player.getUniqueId())) {

            return;
        }

        for (int i = 0; i < lines.size(); i++) {

            HologramLine line = lines.get(i);

            sendPacket(player, spawnLine(line, i));

            sendPacket(player, metadataLine(line));
        }

        if (isTouchable()) {

            sendPacket(player, spawnTouch());

            sendPacket(player, metadataTouch());
        }
    }

    /*
     * ==================================================
     * HIDE
     * ==================================================
     */

    public void hide(Player player) {

        if (player == null) {
            return;
        }

        if (!viewers.remove(player.getUniqueId())) {

            return;
        }

        sendPacket(player, destroy());
    }

    public void hideAll() {

        for (UUID uuid : viewers) {

            Player player = Bukkit.getPlayer(uuid);

            if (player != null && player.isOnline()) {

                sendPacket(player, destroy());
            }
        }

        viewers.clear();
    }

    /*
     * ==================================================
     * REFRESH
     * ==================================================
     */

    public void refresh() {

        for (UUID uuid : viewers) {

            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {

                viewers.remove(uuid);

                continue;
            }

            sendPacket(player, destroy());

            for (int i = 0; i < lines.size(); i++) {

                HologramLine line = lines.get(i);

                sendPacket(player, spawnLine(line, i));

                sendPacket(player, metadataLine(line));
            }

            if (isTouchable()) {

                sendPacket(player, spawnTouch());

                sendPacket(player, metadataTouch());
            }
        }
    }

    /*
     * ==================================================
     * TELEPORT
     * ==================================================
     */

    public void teleport(Location location) {

        if (location == null) {
            return;
        }

        this.location = location.clone();

        for (UUID uuid : viewers) {

            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {

                viewers.remove(uuid);

                continue;
            }

            if (this.location.getWorld() == null || !this.location.getWorld().equals(player.getWorld())) {

                hide(player);

                continue;
            }

            for (int i = 0; i < lines.size(); i++) {

                sendPacket(player, teleportLine(lines.get(i), i));
            }

            if (isTouchable()) {

                sendPacket(player, teleportTouch());
            }
        }
    }

    public void move(Location location) {
        teleport(location);
    }

    /*
     * ==================================================
     * REMOVE
     * ==================================================
     */

    public void remove() {

        hideAll();

        HOLOGRAMS.remove(touchId);
    }

    /*
     * ==================================================
     * STATIC
     * ==================================================
     */

    public static Set<Hologram> getHolograms() {

        return Set.copyOf(HOLOGRAMS.values());
    }

    public static Hologram getHologram(int touchId) {

        return HOLOGRAMS.get(touchId);
    }

    public static void removeAll() {

        for (Hologram hologram : getHolograms()) {

            hologram.remove();
        }

        HOLOGRAMS.clear();
    }

    /*
     * ==================================================
     * SPAWN LINE
     * ==================================================
     */

    private PacketContainer spawnLine(HologramLine line, int index) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, line.entityId());

        packet.getUUIDs().write(0, line.uuid());

        packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);

        Location lineLocation = getLineLocation(index);

        packet.getDoubles().write(0, lineLocation.getX());

        packet.getDoubles().write(1, lineLocation.getY());

        packet.getDoubles().write(2, lineLocation.getZ());

        return packet;
    }

    /*
     * ==================================================
     * LINE METADATA
     * ==================================================
     */

    private PacketContainer metadataLine(HologramLine line) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, line.entityId());

        WrappedDataWatcher watcher = createArmorStandWatcher(line.text());

        packet.getDataValueCollectionModifier().write(0, watcher.toDataValueCollection());

        return packet;
    }

    /*
     * ==================================================
     * SPAWN TOUCH
     * ==================================================
     */

    private PacketContainer spawnTouch() {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, touchId);

        packet.getUUIDs().write(0, touchUuid);

        packet.getEntityTypeModifier().write(0, EntityType.SLIME);

        Location touchLocation = location.clone();

        touchLocation.add(0, TEXT_HEIGHT + TOUCH_HEIGHT, 0);

        packet.getDoubles().write(0, touchLocation.getX());

        packet.getDoubles().write(1, touchLocation.getY());

        packet.getDoubles().write(2, touchLocation.getZ());

        return packet;
    }

    /*
     * ==================================================
     * TOUCH METADATA
     * ==================================================
     */

    private PacketContainer metadataTouch() {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, touchId);

        WrappedDataWatcher watcher = createTouchWatcher();

        packet.getDataValueCollectionModifier().write(0, watcher.toDataValueCollection());

        return packet;
    }

    /*
     * ==================================================
     * TELEPORT LINE
     * ==================================================
     */

    private PacketContainer teleportLine(HologramLine line, int index) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, line.entityId());

        Location lineLocation = getLineLocation(index);

        packet.getDoubles().write(0, lineLocation.getX());

        packet.getDoubles().write(1, lineLocation.getY());

        packet.getDoubles().write(2, lineLocation.getZ());

        return packet;
    }

    /*
     * ==================================================
     * TELEPORT TOUCH
     * ==================================================
     */

    private PacketContainer teleportTouch() {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, touchId);

        Location touchLocation = location.clone();

        touchLocation.add(0, TEXT_HEIGHT + TOUCH_HEIGHT, 0);

        packet.getDoubles().write(0, touchLocation.getX());

        packet.getDoubles().write(1, touchLocation.getY());

        packet.getDoubles().write(2, touchLocation.getZ());

        return packet;
    }

    /*
     * ==================================================
     * DESTROY
     * ==================================================
     */

    private PacketContainer destroy() {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getModifier().writeDefaults();

        int[] entityIds = new int[lines.size() + 1];

        for (int i = 0; i < lines.size(); i++) {

            entityIds[i] = lines.get(i).entityId();
        }

        entityIds[lines.size()] = touchId;

        packet.getIntLists().write(0, new it.unimi.dsi.fastutil.ints.IntArrayList(entityIds));

        return packet;
    }

    /*
     * ==================================================
     * ARMOR STAND WATCHER
     * ==================================================
     */

    private WrappedDataWatcher createArmorStandWatcher(String text) {

        if (location.getWorld() == null) {

            throw new IllegalStateException("O mundo do hologram não pode ser nulo.");
        }

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        try {

            armorStand.setInvisible(true);
            armorStand.setMarker(true);
            armorStand.setGravity(false);
            armorStand.setSmall(false);

            if (text != null && !text.isEmpty()) {

                Component component = parseText(text);

                armorStand.customName(component);

                armorStand.setCustomNameVisible(true);

            } else {

                armorStand.setCustomNameVisible(false);
            }

            return new WrappedDataWatcher(armorStand);

        } finally {

            armorStand.remove();
        }
    }

    /*
     * ==================================================
     * TOUCH WATCHER
     * ==================================================
     */

    private WrappedDataWatcher createTouchWatcher() {

        if (location.getWorld() == null) {

            throw new IllegalStateException("O mundo do hologram não pode ser nulo.");
        }

        Slime slime = (Slime) location.getWorld().spawnEntity(location, EntityType.SLIME);

        try {

            slime.setInvisible(true);
            slime.setAI(false);
            slime.setGravity(false);
            slime.setSize(2);

            return new WrappedDataWatcher(slime);

        } finally {

            slime.remove();
        }
    }

    /*
     * ==================================================
     * LINE LOCATION
     * ==================================================
     */

    private Location getLineLocation(int index) {

        Location lineLocation = location.clone();

        /*
         * A primeira linha fica no topo.
         * As próximas descem.
         */
        lineLocation.add(0, TEXT_HEIGHT - (index * LINE_DISTANCE), 0);

        return lineLocation;
    }

    /*
     * ==================================================
     * SEND
     * ==================================================
     */

    private void sendPacket(Player player, PacketContainer packet) {

        try {

            protocolManager.sendServerPacket(player, packet);

        } catch (Exception exception) {

            Bukkit.getLogger().warning("Erro ao enviar pacote do Hologram: " + exception.getMessage());
        }
    }

    private Component parseText(String text) {

        if (text.indexOf('§') >= 0) {

            return LegacyComponentSerializer.legacySection().deserialize(text);
        }

        if (text.indexOf('&') >= 0) {

            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }

        return MiniMessage.miniMessage().deserialize(text);
    }

    /*
     * ==================================================
     * LINE
     * ==================================================
     */

    private record HologramLine(int entityId, UUID uuid, String text) {
    }
}