package br.com.laboon.bukkit.gui;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnvilGuiManager implements Listener {

    private final JavaPlugin plugin;
    private final ProtocolManager protocolManager;

    private final Map<UUID, AnvilGui> openGuis = new ConcurrentHashMap<>();

    private final Map<UUID, String> inputTexts = new ConcurrentHashMap<>();

    public AnvilGuiManager(JavaPlugin plugin) {

        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }

        this.plugin = plugin;

        this.protocolManager = ProtocolLibrary.getProtocolManager();

        registerPacketListeners();
    }

    private void registerPacketListeners() {

        /*
         * =========================================================
         * ALTERAÇÃO DO TEXTO DA BIGORNA
         * =========================================================
         */
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.ITEM_NAME) {

            @Override
            public void onPacketReceiving(PacketEvent event) {

                Player player = event.getPlayer();

                if (player == null) {
                    return;
                }

                UUID uuid = player.getUniqueId();

                AnvilGui gui = openGuis.get(uuid);

                if (gui == null) {
                    return;
                }

                String text;

                try {

                    text = event.getPacket().getStrings().read(0);

                } catch (Exception exception) {
                    return;
                }

                if (text == null) {
                    text = "";
                }

                final String finalText = text;

                /*
                 * Guardamos imediatamente.
                 */
                inputTexts.put(uuid, finalText);

                /*
                 * Alterações Bukkit precisam ocorrer
                 * na main thread.
                 */
                plugin.getServer().getScheduler().runTask(plugin, () -> {

                    AnvilGui current = openGuis.get(uuid);

                    if (current == null) {
                        return;
                    }

                    updateResult(player, current, finalText);
                });
            }
        });

        /*
         * =========================================================
         * CLIQUE DO CLIENTE
         * =========================================================
         *
         * Mantemos o InventoryClickEvent como principal mecanismo
         * de confirmação.
         *
         * O WINDOW_CLICK será utilizado apenas como fallback
         * caso o cliente/protocolo não gere corretamente o evento
         * Bukkit.
         */
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Client.WINDOW_CLICK) {

            @Override
            public void onPacketReceiving(PacketEvent event) {

                Player player = event.getPlayer();

                if (player == null) {
                    return;
                }

                UUID uuid = player.getUniqueId();

                if (!openGuis.containsKey(uuid)) {
                    return;
                }

                /*
                 * Não vamos cancelar o pacote aqui.
                 *
                 * O InventoryClickEvent continua sendo
                 * responsável pela confirmação.
                 */
            }
        });
    }

    private void updateResult(Player player, AnvilGui gui, String text) {

        if (player == null || gui == null) {
            return;
        }

        if (gui.getInventory() == null) {
            return;
        }

        /*
         * Precisamos estar realmente em uma AnvilView.
         */
        if (!(player.getOpenInventory() instanceof AnvilView view)) {
            return;
        }

        if (!view.getTopInventory().equals(gui.getInventory())) {
            return;
        }

        String value = text == null ? "" : text.trim();

        /*
         * Sempre configuramos o custo.
         */
        view.setRepairCost(0);
        view.setMaximumRepairCost(40);

        /*
         * Se não existe texto, limpa o resultado.
         */
        if (value.isEmpty()) {

            view.getTopInventory().setItem(2, null);

            player.updateInventory();

            return;
        }

        /*
         * Cria o resultado.
         */
        ItemStack result = new ItemStack(Material.PAPER);

        ItemMeta meta = result.getItemMeta();

        if (meta != null) {

            meta.displayName(Component.text(value));

            result.setItemMeta(meta);
        }

        /*
         * Slot 2 = resultado.
         */
        view.getTopInventory().setItem(2, result);

        view.setRepairCost(0);
        view.setMaximumRepairCost(40);

        /*
         * Força atualização para o cliente.
         */
        player.updateInventory();
    }

    public void open(Player player, AnvilGui gui) {

        if (player == null || gui == null) {
            return;
        }

        UUID uuid = player.getUniqueId();

        /*
         * Remove GUI anterior.
         */
        AnvilGui previous = openGuis.put(uuid, gui);

        if (previous != null) {
            inputTexts.remove(uuid);
        }

        /*
         * Abre.
         */
        gui.open(player);

        /*
         * Guarda o texto inicial.
         */
        String initialText = gui.getInitialText();

        if (initialText == null) {
            initialText = "";
        }

        inputTexts.put(uuid, initialText);

        final String finalInitialText = initialText;

        /*
         * Espera o inventário estar realmente aberto.
         */
        plugin.getServer().getScheduler().runTask(plugin, () -> {

            AnvilGui current = openGuis.get(uuid);

            if (current == null) {
                return;
            }

            updateResult(player, current, finalInitialText);
        });
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {

        /*
         * Paper 1.21.11 fornece o AnvilView
         * através do PrepareAnvilEvent.
         */
        AnvilView view = event.getView();

        if (!(view.getPlayer() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        AnvilGui gui = openGuis.get(uuid);

        if (gui == null) {
            return;
        }

        if (!view.getTopInventory().equals(gui.getInventory())) {
            return;
        }

        String text = inputTexts.getOrDefault(uuid, "");

        text = text.trim();

        view.setRepairCost(0);
        view.setMaximumRepairCost(40);

        if (text.isEmpty()) {

            event.setResult(null);

            return;
        }

        ItemStack result = createResult(text);

        event.setResult(result);
    }

    private ItemStack createResult(String text) {

        ItemStack result = new ItemStack(Material.PAPER);

        ItemMeta meta = result.getItemMeta();

        if (meta != null) {

            meta.displayName(Component.text(text));

            result.setItemMeta(meta);
        }

        return result;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        AnvilGui gui = openGuis.get(uuid);

        if (gui == null) {
            return;
        }

        if (!event.getView().getTopInventory().equals(gui.getInventory())) {
            return;
        }

        /*
         * Bloqueia movimentação dos itens.
         */
        event.setCancelled(true);

        /*
         * Precisamos somente do slot de resultado.
         */
        if (event.getRawSlot() != 2) {
            return;
        }

        /*
         * NÃO usamos mais getRenameText()
         * para decidir o texto.
         *
         * O texto capturado pelo ProtocolLib
         * é nossa fonte principal.
         */
        String text = inputTexts.getOrDefault(uuid, "");

        text = text.trim();

        if (text.isEmpty()) {

            player.sendMessage(Component.text("§cDigite um texto."));

            return;
        }

        /*
         * Executa a ação.
         */
        gui.getConsumer().accept(text);

        /*
         * Fecha.
         */
        close(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        AnvilGui gui = openGuis.get(uuid);

        if (gui == null) {
            return;
        }

        if (!event.getView().getTopInventory().equals(gui.getInventory())) {
            return;
        }

        openGuis.remove(uuid, gui);

        inputTexts.remove(uuid);
    }

    public void close(Player player) {

        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();

        openGuis.remove(uuid);
        inputTexts.remove(uuid);

        player.closeInventory();
    }

    public AnvilGui get(Player player) {

        if (player == null) {
            return null;
        }

        return openGuis.get(player.getUniqueId());
    }

    public boolean isOpen(Player player) {

        if (player == null) {
            return false;
        }

        return openGuis.containsKey(player.getUniqueId());
    }

    public String getInput(Player player) {

        if (player == null) {
            return "";
        }

        return inputTexts.getOrDefault(player.getUniqueId(), "");
    }

    public void shutdown() {

        protocolManager.removePacketListeners(plugin);

        openGuis.clear();
        inputTexts.clear();
    }
}