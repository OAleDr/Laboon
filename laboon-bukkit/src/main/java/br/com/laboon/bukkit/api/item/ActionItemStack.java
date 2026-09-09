package br.com.laboon.bukkit.api.item;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ActionItemStack {

    private static final Map<Integer, InteractHandler> HANDLERS = new ConcurrentHashMap<>();
    private static final AtomicInteger HANDLER_ID = new AtomicInteger();

    private static NamespacedKey HANDLER_KEY;

    private ActionItemStack() {
    }

    /**
     * Deve ser chamado no onEnable() do plugin.
     */
    public static void init(JavaPlugin plugin) {
        HANDLER_KEY = new NamespacedKey(plugin, "interact_handler");
    }

    public static int register(InteractHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        for (Map.Entry<Integer, InteractHandler> entry : HANDLERS.entrySet()) {
            if (entry.getValue() == handler) {
                return entry.getKey();
            }
        }

        int id = HANDLER_ID.incrementAndGet();

        HANDLERS.put(id, handler);

        return id;
    }

    public static void unregister(int id) {
        HANDLERS.remove(id);
    }

    public static void unregister(InteractHandler handler) {
        HANDLERS.entrySet().removeIf(entry -> entry.getValue() == handler);
    }

    /**
     * Adiciona o handler ao ItemStack usando PersistentDataContainer.
     */
    public static ItemStack setInteractHandler(ItemStack stack, InteractHandler handler) {

        if (stack == null || stack.getType().isAir()) {
            return stack;
        }

        if (HANDLER_KEY == null) {
            throw new IllegalStateException("ActionItemStack.init(plugin) não foi chamado.");
        }

        int id = register(handler);

        ItemStack result = stack.clone();

        var meta = result.getItemMeta();

        meta.getPersistentDataContainer().set(HANDLER_KEY, PersistentDataType.INTEGER, id);

        result.setItemMeta(meta);

        return result;
    }

    /**
     * Retorna o handler associado ao item.
     */
    public static InteractHandler getHandler(ItemStack stack) {

        if (stack == null || stack.getType().isAir()) {
            return null;
        }

        if (HANDLER_KEY == null) {
            return null;
        }

        PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();

        Integer id = pdc.get(HANDLER_KEY, PersistentDataType.INTEGER);

        if (id == null) {
            return null;
        }

        return HANDLERS.get(id);
    }

    /**
     * Executa o handler associado ao item.
     */
    public static boolean handle(Player player, ItemStack item, ItemAction action) {
        return handle(player, null, item, action);
    }

    public static boolean handle(Player player, Player player1, ItemStack item, ItemAction action) {

        InteractHandler handler = getHandler(item);

        if (handler == null) {
            return false;
        }

        return handler.onInteract(player, player1, item, action);
    }

    public interface InteractHandler {

        boolean onInteract(Player player, Player target, ItemStack item, ItemAction action);
    }
}