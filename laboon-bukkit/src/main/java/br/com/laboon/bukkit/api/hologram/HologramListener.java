package br.com.laboon.bukkit.api.hologram;

import br.com.laboon.bukkit.api.Hologram;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HologramListener implements Listener {

    private static final long CLICK_COOLDOWN = 500L;

    private final JavaPlugin plugin;
    private final ProtocolManager protocolManager;

    private final EntityUsePacketListener packetListener;

    private final Map<String, Long> clickCooldowns = new ConcurrentHashMap<>();

    private BukkitTask visibilityTask;

    public HologramListener(JavaPlugin plugin) {

        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }

        this.plugin = plugin;

        this.protocolManager = ProtocolLibrary.getProtocolManager();

        this.packetListener = new EntityUsePacketListener(plugin);

        protocolManager.addPacketListener(packetListener);
    }

    public void start() {

        if (visibilityTask != null) {
            return;
        }

        visibilityTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateVisibility, 10L, 10L);
    }

    public void stop() {

        if (visibilityTask != null) {

            visibilityTask.cancel();

            visibilityTask = null;
        }

        protocolManager.removePacketListener(packetListener);

        clickCooldowns.clear();
    }

    private void updateVisibility() {

        for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {

            for (Hologram hologram : Hologram.getHolograms()) {

                if (hologram.getLocation().getWorld() == null) {

                    continue;
                }

                if (!hologram.getLocation().getWorld().equals(player.getWorld())) {

                    if (hologram.isShownFor(player)) {
                        hologram.hide(player);
                    }

                    continue;
                }

                double distanceSquared = hologram.getLocation().distanceSquared(player.getLocation());

                double viewDistance = Bukkit.getViewDistance() * 16.0D;

                double viewDistanceSquared = viewDistance * viewDistance;

                if (distanceSquared > viewDistanceSquared) {

                    if (hologram.isShownFor(player)) {
                        hologram.hide(player);
                    }

                } else {

                    if (!hologram.isShownFor(player)) {
                        hologram.show(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        clickCooldowns.keySet().removeIf(key -> key.startsWith(player.getUniqueId().toString() + ":"));

        for (Hologram hologram : Hologram.getHolograms()) {

            if (hologram.isShownFor(player)) {

                hologram.hide(player);
            }
        }
    }

    private final class EntityUsePacketListener extends PacketAdapter {

        private EntityUsePacketListener(JavaPlugin plugin) {

            super(plugin, PacketType.Play.Client.USE_ENTITY);
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {

            Player player = event.getPlayer();

            int entityId;

            try {

                entityId = event.getPacket().getIntegers().read(0);

            } catch (Exception exception) {

                return;
            }

            /*
             * O entityId recebido precisa ser
             * exatamente o touchId do hologram.
             */
            Hologram hologram = Hologram.getHologram(entityId);

            if (hologram == null) {
                return;
            }

            /*
             * O jogador precisa estar vendo
             * aquele hologram.
             */
            if (!hologram.isShownFor(player)) {
                return;
            }

            /*
             * O hologram precisa possuir Action.
             */
            HologramClickAction action = hologram.getClickAction();

            if (action == null) {
                return;
            }

            HologramClickType clickType = resolveClickType(event);

            if (clickType == HologramClickType.UNKNOWN) {

                return;
            }

            /*
             * Evita múltiplos cliques
             * disparados rapidamente.
             */
            String cooldownKey = player.getUniqueId() + ":" + hologram.getTouchId();

            long now = System.currentTimeMillis();

            Long lastClick = clickCooldowns.get(cooldownKey);

            if (lastClick != null && now - lastClick < CLICK_COOLDOWN) {

                return;
            }

            clickCooldowns.put(cooldownKey, now);

            /*
             * Cancela o processamento normal
             * do pacote pelo servidor.
             */
            event.setCancelled(true);

            Bukkit.getScheduler().runTask(plugin, () -> {

                if (!player.isOnline()) {
                    return;
                }

                /*
                 * Confere novamente para evitar
                 * executar Action depois que
                 * o hologram foi removido.
                 */
                Hologram current = Hologram.getHologram(entityId);

                if (current == null) {
                    return;
                }

                HologramClickAction currentAction = current.getClickAction();

                if (currentAction == null) {
                    return;
                }

                currentAction.onClick(player, clickType, entityId);
            });
        }

        private HologramClickType resolveClickType(PacketEvent event) {

            try {

                Object action = event.getPacket().getModifier().read(1);

                if (action == null) {
                    return HologramClickType.UNKNOWN;
                }

                String actionName = action.getClass().getName();

                /*
                 * Minecraft 1.21.11
                 *
                 * Botão esquerdo:
                 * ServerboundInteractPacket$1
                 *
                 * Botão direito:
                 * ServerboundInteractPacket$InteractionAction
                 *
                 * Botão direito mirando uma posição:
                 * ServerboundInteractPacket$InteractionAtLocationAction
                 */

                if (actionName.endsWith("ServerboundInteractPacket$1")) {

                    return HologramClickType.LEFT_CLICK;
                }

                if (actionName.endsWith("InteractionAction")) {

                    return HologramClickType.RIGHT_CLICK;
                }

                if (actionName.endsWith("InteractionAtLocationAction")) {

                    return HologramClickType.RIGHT_CLICK;
                }

            } catch (Exception exception) {

                plugin.getLogger().warning("Erro ao identificar o clique do hologram: " + exception.getMessage());
            }

            return HologramClickType.UNKNOWN;
        }
    }

}