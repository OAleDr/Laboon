package br.com.laboon.bukkit.gui.friend;

import br.com.laboon.bukkit.gui.AnvilGui;
import br.com.laboon.bukkit.gui.AnvilGuiManager;
import br.com.laboon.bukkit.gui.Gui;
import br.com.laboon.bukkit.gui.GuiItem;
import br.com.laboon.bukkit.gui.GuiManager;
import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.friend.Friend;
import br.com.laboon.core.friend.FriendManager;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class FriendGui {

    private static final int ROWS = 6;

    private static final int FRIENDS_PER_PAGE = 21;

    private static final int PREVIOUS_SLOT = 18;

    private static final int NEXT_SLOT = 26;

    private static final int INFORMATION_SLOT = 49;

    private static final int ADD_SLOT = 50;

    private static final int CLOSE_SLOT = 53;

    private static final int[] FRIEND_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final GuiManager guiManager;

    private final AnvilGuiManager anvilGuiManager;

    private final FriendManager friendManager;

    private final AccountManager accountManager;

    public FriendGui(GuiManager guiManager, AnvilGuiManager anvilGuiManager, FriendManager friendManager, AccountManager accountManager) {

        if (guiManager == null) {
            throw new IllegalArgumentException("GuiManager não pode ser nulo.");
        }

        if (anvilGuiManager == null) {
            throw new IllegalArgumentException("AnvilGuiManager não pode ser nulo.");
        }

        if (friendManager == null) {
            throw new IllegalArgumentException("FriendManager não pode ser nulo.");
        }

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        this.guiManager = guiManager;
        this.anvilGuiManager = anvilGuiManager;
        this.friendManager = friendManager;
        this.accountManager = accountManager;
    }

    public void open(Player player) {

        open(player, 0);
    }

    public void open(Player player, int page) {

        if (player == null) {
            return;
        }

        /*
         * =========================
         * AMIGOS
         * =========================
         */

        List<Friend> friends = new ArrayList<>(friendManager.getFriendDetails(player.getUniqueId()));

        /*
         * Ordena pelo nome do jogador.
         */

        friends.sort(Comparator.comparing(friend -> getPlayerName(friend.getUniqueId()), String.CASE_INSENSITIVE_ORDER));

        int totalFriends = friends.size();

        /*
         * =========================
         * PAGINAÇÃO
         * =========================
         */

        int totalPages = Math.max(1, (int) Math.ceil(totalFriends / (double) FRIENDS_PER_PAGE));

        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        /*
         * =========================
         * GUI
         * =========================
         */

        Gui gui = guiManager.create(ROWS, "<aqua>Amigos");

        int start = currentPage * FRIENDS_PER_PAGE;

        int end = Math.min(start + FRIENDS_PER_PAGE, totalFriends);

        int slotIndex = 0;

        /*
         * =========================
         * CABEÇAS DOS AMIGOS
         * =========================
         */

        for (int index = start; index < end; index++) {

            if (slotIndex >= FRIEND_SLOTS.length) {
                break;
            }

            Friend friend = friends.get(index);

            OfflinePlayer friendPlayer = Bukkit.getOfflinePlayer(friend.getUniqueId());

            GuiItem item = createFriendItem(friendPlayer, friend);

            gui.setItem(FRIEND_SLOTS[slotIndex], item);

            slotIndex++;
        }

        /*
         * =========================
         * INFORMAÇÕES
         * =========================
         */

        gui.setItem(INFORMATION_SLOT,

                GuiItem.item(Material.BOOK).name("<aqua>Amigos").lore("<gray>Total: <white>" + totalFriends, "", "<gray>Página: <white>" + (currentPage + 1) + "<gray>/<white>" + totalPages));

        /*
         * =========================
         * PÁGINA ANTERIOR
         * =========================
         */

        if (currentPage > 0) {

            gui.setItem(PREVIOUS_SLOT,

                    GuiItem.item(Material.ARROW).name("<yellow>Página anterior").lore("<gray>Clique para voltar.").onClick(event -> open(event.getPlayer(), currentPage - 1)));
        }

        /*
         * =========================
         * PRÓXIMA PÁGINA
         * =========================
         */

        if (currentPage + 1 < totalPages) {

            gui.setItem(NEXT_SLOT,

                    GuiItem.item(Material.ARROW).name("<yellow>Próxima página").lore("<gray>Clique para avançar.").onClick(event -> open(event.getPlayer(), currentPage + 1)));
        }

        /*
         * =========================
         * ADICIONAR AMIGO
         * =========================
         */

        gui.setItem(ADD_SLOT,

                GuiItem.item(Material.PAPER).name("<green>Adicionar amigo").lore("<gray>Envie uma solicitação", "<gray>de amizade para outro jogador.", "", "<yellow>Clique para adicionar.").onClick(event -> openAddFriend(event.getPlayer())));

        /*
         * =========================
         * FECHAR
         * =========================
         */

        gui.setItem(CLOSE_SLOT,

                GuiItem.item(Material.BARRIER).name("<red>Fechar").lore("<gray>Clique para fechar.").onClick(event -> event.close()));

        guiManager.open(player, gui);
    }

    private GuiItem createFriendItem(OfflinePlayer friendPlayer, Friend friend) {

        UUID friendId = friend.getUniqueId();

        String name = getPlayerName(friendId);

        boolean online = friendPlayer.isOnline();

        Account account = accountManager.get(friendId);

        Group group = account == null ? Group.DEFAULT : account.getGroup();

        String groupName = group == Group.DEFAULT ? "Nenhum" : group.getDisplayName();

        /*
         * =========================
         * CABEÇA
         * =========================
         */

        GuiItem item = GuiItem.item(Material.PLAYER_HEAD).playerHead(friendPlayer);

        item.name("<white>" + name);

        /*
         * =========================
         * INFORMAÇÕES
         * =========================
         */

        if (online) {

            item.lore("", "<gray>Status: <green>Online", "<gray>Grupo: <white>" + groupName);

        } else {

            item.lore("", "<gray>Status: <red>Offline", "<gray>Grupo: <white>" + groupName);
        }

        /*
         * Data real armazenada no Redis.
         */

        item.lore("", "<gray>Adicionado: <white>" + DATE_FORMAT.format(friend.getAddedAt()), "", "<yellow>Clique para ver opções.");

        /*
         * =========================
         * AÇÃO
         * =========================
         */

        item.onClick(event -> {

            /*
             * As opções do amigo serão
             * implementadas posteriormente.
             */

            event.close();
        });

        return item;
    }

    private void openAddFriend(Player player) {

        if (player == null) {
            return;
        }

        AnvilGui anvil = new AnvilGui("<green>Adicionar amigo", "", text -> sendFriendRequest(player, text));

        anvilGuiManager.open(player, anvil);
    }

    private void sendFriendRequest(Player sender, String input) {

        String name = input == null ? "" : input.trim();

        if (name.isEmpty()) {

            sender.sendMessage(Component.text("§cDigite o nome do jogador."));

            return;
        }

        /*
         * Primeiro procura um jogador online.
         */

        Player onlinePlayer = Bukkit.getPlayerExact(name);

        OfflinePlayer target;

        if (onlinePlayer != null) {

            target = onlinePlayer;

        } else {

            target = Bukkit.getOfflinePlayer(name);
        }

        /*
         * Jogador nunca conhecido pelo servidor.
         */

        if (!target.isOnline() && !target.hasPlayedBefore()) {

            sender.sendMessage(Component.text("§cJogador não encontrado."));

            return;
        }

        UUID targetId = target.getUniqueId();

        if (targetId == null) {

            sender.sendMessage(Component.text("§cJogador não encontrado."));

            return;
        }

        /*
         * Não pode adicionar a si mesmo.
         */

        if (targetId.equals(sender.getUniqueId())) {

            sender.sendMessage(Component.text("§cVocê não pode adicionar a si mesmo."));

            return;
        }

        /*
         * Já são amigos.
         */

        if (friendManager.isFriend(sender.getUniqueId(), targetId)) {

            sender.sendMessage(Component.text("§cVocês já são amigos."));

            return;
        }

        /*
         * Envia a solicitação.
         */

        boolean sent = friendManager.sendRequest(sender.getUniqueId(), targetId);

        if (!sent) {

            sender.sendMessage(Component.text("§cNão foi possível enviar a solicitação."));

            return;
        }

        sender.sendMessage(Component.text("§aSolicitação de amizade enviada para " + name + "."));

        /*
         * Se estiver online, recebe a notificação.
         */

        if (onlinePlayer != null) {

            onlinePlayer.sendMessage(Component.text("§aVocê recebeu uma solicitação de amizade de " + sender.getName() + "."));
        }
    }

    private String getPlayerName(UUID uniqueId) {

        Account account = accountManager.get(uniqueId);

        if (account != null && account.getName() != null && !account.getName().isBlank()) {

            return account.getName();
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);

        if (player.getName() != null) {

            return player.getName();
        }

        return uniqueId.toString();
    }
}