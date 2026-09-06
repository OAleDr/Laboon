package br.com.laboon.bukkit.api.npc;

import br.com.laboon.bukkit.api.CooldownAPI;
import br.com.laboon.bukkit.api.cooldown.types.SilentCooldown;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NPCListener implements Listener {

    private static final double VIEW_DISTANCE = 64.0D;

    private static final String COOLDOWN_PREFIX = "useNPC:";

    private static final long CLICK_COOLDOWN = 1L;

    private final JavaPlugin plugin;

    private final ProtocolManager protocolManager;

    private final Map<UUID, Map<Integer, Boolean>> visibleNPCs = new ConcurrentHashMap<>();

    private PacketAdapter packetListener;

    private BukkitTask visibilityTask;

    public NPCListener(JavaPlugin plugin) {

        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }

        this.plugin = plugin;

        this.protocolManager = ProtocolLibrary.getProtocolManager();

        registerPacketListener();
    }

    /*
     * ==========================
     * PACKET LISTENER
     * ==========================
     */

    private void registerPacketListener() {

        packetListener = new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {

            @Override
            public void onPacketReceiving(PacketEvent event) {

                Player player = event.getPlayer();

                if (player == null || !player.isOnline()) {
                    return;
                }

                int entityId;

                try {

                    entityId = event.getPacket().getIntegers().read(0);

                } catch (Exception exception) {

                    return;
                }

                NPC npc = NPC.getNPC(entityId);

                if (npc == null) {
                    return;
                }

                if (!npc.isTouchable()) {
                    return;
                }

                NPCClickType clickType = getClickType(event);

                if (clickType == NPCClickType.UNKNOWN) {
                    return;
                }

                /*
                 * USE_ENTITY chega pela thread
                 * de rede.
                 */
                event.setCancelled(true);

                /*
                 * CooldownAPI e a ação do NPC
                 * precisam ser executados na
                 * thread principal.
                 */
                Bukkit.getScheduler().runTask(plugin, () -> {

                    if (!player.isOnline()) {
                        return;
                    }

                    NPC current = NPC.getNPC(entityId);

                    if (current == null) {
                        return;
                    }

                    NPCClickAction action = current.getClickAction();

                    if (action == null) {
                        return;
                    }

                    String cooldownName = COOLDOWN_PREFIX + current.getEntityId();

                    if (CooldownAPI.hasCooldown(player, cooldownName)) {
                        return;
                    }

                    CooldownAPI.addCooldown(player, new SilentCooldown(cooldownName, CLICK_COOLDOWN));

                    action.onClick(player, clickType, current);
                });
            }
        };

        protocolManager.addPacketListener(packetListener);
    }

    /*
     * ==========================
     * CLICK TYPE
     * ==========================
     */

    private NPCClickType getClickType(PacketEvent event) {

        /*
         * Tentativa normal do ProtocolLib.
         */
        try {

            EnumWrappers.EntityUseAction action = event.getPacket().getEnumEntityUseActions().read(0).getAction();

            return switch (action) {

                case ATTACK -> NPCClickType.LEFT_CLICK;

                case INTERACT, INTERACT_AT -> NPCClickType.RIGHT_CLICK;

                default -> NPCClickType.UNKNOWN;
            };

        } catch (Exception ignored) {
            // Fallback abaixo.
        }

        /*
         * Fallback para Minecraft 1.21.x.
         */
        try {

            Object action = event.getPacket().getModifier().read(1);

            if (action == null) {
                return NPCClickType.UNKNOWN;
            }

            String className = action.getClass().getName();

            if (className.endsWith("ServerboundInteractPacket$1")) {
                return NPCClickType.LEFT_CLICK;
            }

            if (className.endsWith("InteractionAction")) {
                return NPCClickType.RIGHT_CLICK;
            }

            if (className.endsWith("InteractionAtLocationAction")) {
                return NPCClickType.RIGHT_CLICK;
            }

        } catch (Exception ignored) {
        }

        return NPCClickType.UNKNOWN;
    }

    /*
     * ==========================
     * START
     * ==========================
     */

    public void start() {

        if (visibilityTask != null) {
            return;
        }

        visibilityTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateVisibility, 1L, 10L);
    }

    /*
     * ==========================
     * STOP
     * ==========================
     */

    public void stop() {

        if (visibilityTask != null) {

            visibilityTask.cancel();

            visibilityTask = null;
        }

        if (packetListener != null) {

            protocolManager.removePacketListener(packetListener);

            packetListener = null;
        }

        visibleNPCs.clear();
    }

    /*
     * ==========================
     * VISIBILITY
     * ==========================
     */

    private void updateVisibility() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!player.isOnline()) {
                continue;
            }

            Map<Integer, Boolean> playerNPCs = visibleNPCs.computeIfAbsent(player.getUniqueId(), key -> new ConcurrentHashMap<>());

            for (NPC npc : NPC.getNPCs().values()) {

                Location npcLocation = npc.getLocation();

                if (npcLocation.getWorld() == null) {
                    continue;
                }

                /*
                 * Mundo diferente.
                 */
                if (!npcLocation.getWorld().equals(player.getWorld())) {

                    if (playerNPCs.remove(npc.getEntityId()) != null) {

                        sendDestroy(npc, player);
                    }

                    continue;
                }

                double distanceSquared = player.getLocation().distanceSquared(npcLocation);

                boolean shouldSee = distanceSquared <= getViewDistanceSquared();

                boolean currentlyVisible = playerNPCs.containsKey(npc.getEntityId());

                if (shouldSee && !currentlyVisible) {

                    sendSpawn(npc, player);

                    playerNPCs.put(npc.getEntityId(), Boolean.TRUE);

                } else if (!shouldSee && currentlyVisible) {

                    sendDestroy(npc, player);

                    playerNPCs.remove(npc.getEntityId());
                }
            }
        }
    }

    /*
     * ==========================
     * SPAWN
     * ==========================
     */

    static void sendSpawn(NPC npc, Player player) {

        WrappedGameProfile profile = npc.getProfile();

        /*
         * ==========================
         * SKIN
         * ==========================
         *
         * Aplicamos a textura diretamente
         * no GameProfile interno.
         *
         * Não usamos:
         *
         * profile.getProperties()
         *
         * porque essa API está quebrada
         * no ProtocolLib 5.4.0 +
         * Paper 1.21.11.
         */

        try {

            applySkin(profile, npc.getSkinProperty());

        } catch (Exception exception) {

            npc.getPlugin().getLogger().warning("Não foi possível aplicar a skin do NPC " + npc.getEntityId() + ": " + exception.getMessage());
        }

        /*
         * ==========================
         * PLAYER INFO
         * ==========================
         */

        try {

            var info = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);

            PlayerInfoData data = new PlayerInfoData(npc.getUniqueId(), 0, true, EnumWrappers.NativeGameMode.SURVIVAL, profile, null);

            Object genericData = PlayerInfoData.getConverter().getGeneric(data);

            setPlayerInfoPacketFields(info.getHandle(), genericData);

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, info);

        } catch (Exception exception) {

            npc.getPlugin().getLogger().warning("Erro ao enviar PLAYER_INFO do NPC " + npc.getEntityId() + ": " + exception.getMessage());

            return;
        }

        /*
         * ==========================
         * SPAWN ENTITY
         * ==========================
         */

        try {

            var spawn = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

            Location location = npc.getLocation();

            /*
             * Entity ID
             */
            spawn.getIntegers().write(0, npc.getEntityId());

            /*
             * UUID
             */
            spawn.getUUIDs().write(0, npc.getUniqueId());

            /*
             * PLAYER
             */
            spawn.getEntityTypeModifier().write(0, EntityType.PLAYER);

            /*
             * Coordenadas
             */
            spawn.getDoubles().write(0, location.getX()).write(1, location.getY()).write(2, location.getZ());

            /*
             * Rotação
             */
            byte yaw = (byte) (location.getYaw() * 256.0F / 360.0F);

            byte pitch = (byte) (location.getPitch() * 256.0F / 360.0F);

            spawn.getBytes().write(0, pitch).write(1, yaw).write(2, yaw);

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawn);

        } catch (Exception exception) {

            npc.getPlugin().getLogger().warning("Erro ao enviar SPAWN_ENTITY do NPC " + npc.getEntityId() + ": " + exception.getMessage());

            return;
        }

        /*
         * ==========================
         * REMOVE DA TAB
         * ==========================
         */

        Bukkit.getScheduler().runTaskLater(npc.getPlugin(), () -> removeFromTab(npc, player), 20L);
    }

    /*
     * ==========================
     * APPLY SKIN
     * ==========================
     */

    private static void applySkin(WrappedGameProfile wrappedProfile, WrappedSignedProperty property) throws Exception {

        if (wrappedProfile == null || property == null) {
            return;
        }

        Object handle = getWrappedHandle(wrappedProfile);

        if (handle == null) {

            throw new IllegalStateException("Não foi possível obter o GameProfile interno.");
        }

        Object properties = findPropertiesMap(handle);

        if (properties == null) {

            throw new IllegalStateException("Não foi possível localizar as propriedades do GameProfile.");
        }

        Object nativeProperty = convertSignedProperty(property);

        if (nativeProperty == null) {

            throw new IllegalStateException("Não foi possível converter a propriedade textures.");
        }

        java.lang.reflect.Method putMethod = null;

        for (java.lang.reflect.Method method : properties.getClass().getMethods()) {

            if (method.getName().equals("put") && method.getParameterCount() == 2) {

                putMethod = method;

                break;
            }
        }

        if (putMethod == null) {

            throw new IllegalStateException("Não foi possível localizar put() nas propriedades.");
        }

        putMethod.invoke(properties, "textures", nativeProperty);
    }

    /*
     * ==========================
     * GET WRAPPED HANDLE
     * ==========================
     */

    private static Object getWrappedHandle(WrappedGameProfile profile) throws Exception {

        /*
         * Tentativa 1:
         * método getHandle().
         */
        try {

            Method method = profile.getClass().getMethod("getHandle");

            method.setAccessible(true);

            Object handle = method.invoke(profile);

            if (handle != null) {
                return handle;
            }

        } catch (Exception ignored) {
        }

        /*
         * Tentativa 2:
         * procurar o campo handle nas
         * classes da hierarquia.
         */
        Class<?> type = profile.getClass();

        while (type != null) {

            for (Field field : type.getDeclaredFields()) {

                if (field.getName().equalsIgnoreCase("handle")) {

                    field.setAccessible(true);

                    Object handle = field.get(profile);

                    if (handle != null) {
                        return handle;
                    }
                }
            }

            type = type.getSuperclass();
        }

        return null;
    }

    /*
     * ==========================
     * FIND PROPERTIES MAP
     * ==========================
     */

    private static Object findPropertiesMap(Object handle) throws IllegalAccessException {

        Class<?> type = handle.getClass();

        while (type != null) {

            for (Field field : type.getDeclaredFields()) {

                field.setAccessible(true);

                Object value = field.get(handle);

                if (value == null) {
                    continue;
                }

                /*
                 * Procuramos algo que possua
                 * put(Object, Object).
                 */
                for (Method method : value.getClass().getMethods()) {

                    if (method.getName().equals("put") && method.getParameterCount() == 2) {

                        return value;
                    }
                }
            }

            type = type.getSuperclass();
        }

        return null;
    }

    /*
     * ==========================
     * CONVERT PROPERTY
     * ==========================
     */

    private static Object convertSignedProperty(WrappedSignedProperty property) throws Exception {

        if (property == null) {
            return null;
        }

        String name = property.getName();

        String value = property.getValue();

        String signature = property.getSignature();

        Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");

        if (signature != null && !signature.isBlank()) {

            return propertyClass.getConstructor(String.class, String.class, String.class).newInstance(name, value, signature);
        }

        return propertyClass.getConstructor(String.class, String.class).newInstance(name, value);
    }

    /*
     * ==========================
     * PLAYER INFO RAW FIELDS
     * ==========================
     */

    private static void setPlayerInfoPacketFields(Object packet, Object playerInfoData) throws Exception {

        if (packet == null) {

            throw new IllegalArgumentException("Packet não pode ser nulo.");
        }

        if (playerInfoData == null) {

            throw new IllegalArgumentException("PlayerInfoData não pode ser nulo.");
        }

        Class<?> packetClass = packet.getClass();

        Field actionsField = null;

        Field entriesField = null;

        /*
         * Procuramos os campos pelo tipo,
         * e não pelo nome obfuscado.
         */
        for (Field field : packetClass.getDeclaredFields()) {

            Class<?> type = field.getType();

            if (actionsField == null && EnumSet.class.isAssignableFrom(type)) {

                actionsField = field;
            }

            if (entriesField == null && List.class.isAssignableFrom(type)) {

                entriesField = field;
            }
        }

        if (actionsField == null) {

            throw new IllegalStateException("Campo EnumSet do PLAYER_INFO não encontrado.");
        }

        if (entriesField == null) {

            throw new IllegalStateException("Campo List do PLAYER_INFO não encontrado.");
        }

        actionsField.setAccessible(true);

        entriesField.setAccessible(true);

        /*
         * Converte ADD_PLAYER para o enum NMS.
         */
        EquivalentConverter<EnumWrappers.PlayerInfoAction> converter = EnumWrappers.getPlayerInfoActionConverter();

        Object nativeAddPlayer = converter.getGeneric(EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        if (!(nativeAddPlayer instanceof Enum<?> nativeAction)) {

            throw new IllegalStateException("Não foi possível converter ADD_PLAYER.");
        }

        /*
         * Cria EnumSet da classe NMS correta.
         */
        @SuppressWarnings({"rawtypes", "unchecked"}) EnumSet nativeActions = EnumSet.noneOf((Class<? extends Enum>) nativeAction.getDeclaringClass());

        nativeActions.add(nativeAction);

        /*
         * Lista NMS contendo a Entry.
         */
        List<Object> entries = new ArrayList<>();

        entries.add(playerInfoData);

        /*
         * Escreve diretamente nos campos
         * do ClientboundPlayerInfoUpdatePacket.
         */
        actionsField.set(packet, nativeActions);

        entriesField.set(packet, entries);
    }

    /*
     * ==========================
     * REMOVE FROM TAB
     * ==========================
     */

    private static void removeFromTab(NPC npc, Player player) {

        if (!player.isOnline()) {
            return;
        }

        try {

            var remove = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);

            remove.getUUIDLists().write(0, List.of(npc.getUniqueId()));

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, remove);

        } catch (Exception exception) {

            npc.getPlugin().getLogger().warning("Erro ao remover NPC da TAB: " + exception.getMessage());
        }
    }

    /*
     * ==========================
     * DESTROY
     * ==========================
     */

    static void sendDestroy(NPC npc, Player player) {

        if (!player.isOnline()) {
            return;
        }

        try {

            var destroy = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

            destroy.getIntLists().write(0, List.of(npc.getEntityId()));

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroy);

        } catch (Exception exception) {

            npc.getPlugin().getLogger().warning("Erro ao destruir NPC: " + exception.getMessage());
        }
    }

    /*
     * ==========================
     * PLAYER QUIT
     * ==========================
     */

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        visibleNPCs.remove(event.getPlayer().getUniqueId());
    }

    /*
     * ==========================
     * VIEW DISTANCE
     * ==========================
     */

    public static double getViewDistanceSquared() {

        return VIEW_DISTANCE * VIEW_DISTANCE;
    }
}