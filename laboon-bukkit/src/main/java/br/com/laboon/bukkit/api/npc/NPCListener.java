package br.com.laboon.bukkit.api.npc;

import br.com.laboon.bukkit.api.CooldownAPI;
import br.com.laboon.bukkit.api.cooldown.types.SilentCooldown;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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
    private final Map<UUID, Map<Integer, Boolean>> visibleNPCs = new ConcurrentHashMap<>();
    private PacketAdapter packetListener;
    private BukkitTask visibilityTask;

    public NPCListener(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }
        this.plugin = plugin;
        registerPacketListener();
    }

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
                if (npc == null || !npc.isTouchable()) {
                    return;
                }
                NPCClickType clickType = getClickType(event);
                if (clickType == NPCClickType.UNKNOWN) {
                    return;
                }
                event.setCancelled(true);
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
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
    }

    private NPCClickType getClickType(PacketEvent event) {
        try {
            EnumWrappers.EntityUseAction action = event.getPacket().getEnumEntityUseActions().read(0).getAction();
            return switch (action) {
                case ATTACK -> NPCClickType.LEFT_CLICK;
                case INTERACT, INTERACT_AT -> NPCClickType.RIGHT_CLICK;
                default -> NPCClickType.UNKNOWN;
            };
        } catch (Exception exception) {
            return NPCClickType.UNKNOWN;
        }
    }

    public void start() {
        if (visibilityTask != null) {
            return;
        }
        visibilityTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateVisibility, 1L, 10L);
    }

    public void stop() {
        if (visibilityTask != null) {
            visibilityTask.cancel();
            visibilityTask = null;
        }
        if (packetListener != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
            packetListener = null;
        }
        visibleNPCs.clear();
    }

    private void updateVisibility() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOnline()) {
                continue;
            }
            Map<Integer, Boolean> playerNPCs = visibleNPCs.computeIfAbsent(player.getUniqueId(), key -> new ConcurrentHashMap<>());
            for (NPC npc : NPC.getNPCs().values()) {
                Location npcLocation = npc.getLocation();
                if (npcLocation == null || npcLocation.getWorld() == null) {
                    continue;
                }
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

    static void sendSpawn(NPC npc, Player player) {
        if (npc == null || player == null || !player.isOnline()) {
            return;
        }
        Location location = npc.getLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }
        WrappedGameProfile profile = npc.getProfile();
        if (profile == null) {
            npc.getPlugin().getLogger().warning("NPC " + npc.getEntityId() + " não possui GameProfile.");
            return;
        }
        UUID uuid = profile.getUUID();
        try { /* * 1. Envia o GameProfile. * * É aqui que o cliente recebe a propriedade * "textures" quando o Profile possui uma skin. */
            sendPlayerInfo(player, profile, npc, uuid); /* * 2. Cria a entidade PLAYER. * * O UUID precisa ser exatamente o mesmo * UUID utilizado no GameProfile. */
            sendSpawnEntity(player, npc, uuid, location); /* * 3. Depois que o cliente recebeu o profile * e criou a entidade, removemos da TAB. */
            Bukkit.getScheduler().runTaskLater(npc.getPlugin(), () -> removeFromTab(npc, player, uuid), 2L);
        } catch (Exception exception) {
            npc.getPlugin().getLogger().warning("Erro ao spawnar NPC " + npc.getEntityId() + ": " + exception.getMessage());
        }
    }

    private static void sendPlayerInfo(Player player, WrappedGameProfile profile, NPC npc, UUID uuid) throws Exception {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        var packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getModifier().writeDefaults(); /* * Mesmo conjunto utilizado pelo NPC-Lib * para inicializar o PlayerInfo. */
        packet.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER, EnumWrappers.PlayerInfoAction.UPDATE_LISTED, EnumWrappers.PlayerInfoAction.UPDATE_LATENCY, EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE, EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME));
        PlayerInfoData data = new PlayerInfoData(uuid, 0, true, EnumWrappers.NativeGameMode.SURVIVAL, profile, WrappedChatComponent.fromText(npc.getNpcProfile().getName()));
        packet.getPlayerInfoDataLists().write(1, List.of(data));
        protocolManager.sendServerPacket(player, packet);
    }

    private static void sendSpawnEntity(Player player, NPC npc, UUID uuid, Location location) throws Exception {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        var packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        packet.getModifier().writeDefaults(); /* * Entity ID */
        packet.getIntegers().write(0, npc.getEntityId()); /* * Object data */
        packet.getIntegers().write(1, 0); /* * UUID do GameProfile. */
        packet.getUUIDs().write(0, uuid); /* * Tipo PLAYER. */
        packet.getEntityTypeModifier().write(0, EntityType.PLAYER); /* * Posição. */
        packet.getDoubles().write(0, location.getX()).write(1, location.getY()).write(2, location.getZ());
        byte yaw = toAngle(location.getYaw());
        byte pitch = toAngle(location.getPitch()); /* * SpawnEntity: * * 0 = pitch * 1 = yaw * 2 = head yaw */
        packet.getBytes().write(0, pitch).write(1, yaw).write(2, yaw);
        protocolManager.sendServerPacket(player, packet);
    }

    private static byte toAngle(float angle) {
        return (byte) (angle * 256.0F / 360.0F);
    }

    private static void removeFromTab(NPC npc, Player player, UUID uuid) {
        if (npc == null || player == null || !player.isOnline()) {
            return;
        }
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            var packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
            packet.getUUIDLists().write(0, List.of(uuid));
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception exception) {
            npc.getPlugin().getLogger().warning("Erro ao remover NPC da TAB " + npc.getEntityId() + ": " + exception.getMessage());
        }
    }

    static void sendDestroy(NPC npc, Player player) {
        if (npc == null || player == null || !player.isOnline()) {
            return;
        }
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            var packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntLists().write(0, List.of(npc.getEntityId()));
            protocolManager.sendServerPacket(player, packet);
            removeFromTab(npc, player, npc.getProfile().getUUID());
        } catch (Exception exception) {
            npc.getPlugin().getLogger().warning("Erro ao destruir NPC " + npc.getEntityId() + ": " + exception.getMessage());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        visibleNPCs.remove(event.getPlayer().getUniqueId());
    }

    public static double getViewDistanceSquared() {
        return VIEW_DISTANCE * VIEW_DISTANCE;
    }
}