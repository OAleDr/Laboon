package br.com.laboon.velocity.command.commands;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;
import br.com.laboon.core.friend.FriendManager;
import br.com.laboon.core.friend.FriendRequest;
import br.com.laboon.velocity.command.VelocityCommandArgs;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class FriendCommand implements CommandClass {

    private final ProxyServer proxyServer;
    private final FriendManager friendManager;
    private final AccountManager accountManager;

    public FriendCommand(ProxyServer proxyServer, FriendManager friendManager, AccountManager accountManager) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (friendManager == null) {
            throw new IllegalArgumentException("FriendManager não pode ser nulo.");
        }

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.friendManager = friendManager;
        this.accountManager = accountManager;
    }

    @Override
    public Completer getCompleter() {

        return args -> {

            List<String> commands = List.of("aceitar", "cancelar", "negar", "remover", "lista", "pedidos", "status");

            if (args.size() == 0) {
                return onlinePlayers("");
            }

            if (args.size() == 1) {

                String input = args.get(0) == null ? "" : args.get(0).toLowerCase(Locale.ROOT);

                List<String> result = new ArrayList<>(filter(commands, input));

                result.addAll(onlinePlayers(input));

                return result.stream().distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
            }

            String subcommand = args.get(0);

            if (subcommand == null) {
                return List.of();
            }

            if (subcommand.equalsIgnoreCase("aceitar") || subcommand.equalsIgnoreCase("negar") || subcommand.equalsIgnoreCase("cancelar") || subcommand.equalsIgnoreCase("remover")) {

                return onlinePlayers(args.get(1));
            }

            return List.of();
        };
    }

    @Command(name = "amigo", aliases = {"friend"}, description = "Gerencia sua lista de amigos.", usage = "/amigo <jogador|aceitar|cancelar|negar|remover|lista|pedidos|status>", subcommands = {"aceitar", "cancelar", "negar", "remover", "lista", "pedidos", "status"})
    public void execute(VelocityCommandArgs args) {

        if (!args.isPlayer()) {

            args.sendMessage("§cApenas jogadores podem usar este comando.");

            return;
        }

        Player player = args.getSender().getPlayer().orElse(null);

        if (player == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        if (args.size() == 0) {

            sendHelp(args);

            return;
        }

        String first = args.get(0);

        if (first == null || first.isBlank()) {

            sendHelp(args);

            return;
        }

        switch (first.toLowerCase(Locale.ROOT)) {

            case "aceitar" -> accept(args, player);

            case "cancelar" -> cancel(args, player);

            case "negar" -> deny(args, player);

            case "remover" -> remove(args, player);

            case "lista" -> list(args, player);

            case "pedidos" -> requests(args, player);

            case "status" -> status(args, player);

            default -> request(args, player);
        }
    }

    /*
     * =========================
     * REQUEST
     * =========================
     */

    private void request(VelocityCommandArgs args, Player player) {

        String targetName = args.get(0);

        Account target = findAccount(targetName);

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        try {

            friendManager.sendRequest(player.getUniqueId(), target.getUniqueId());

            args.sendMessage("§aSolicitação enviada para §f" + target.getName() + "§a.");

            Player targetPlayer = proxyServer.getPlayer(target.getUniqueId()).orElse(null);

            if (targetPlayer != null) {

                targetPlayer.sendMessage(Component.text("§e§lAMIZADE §8» §f" + player.getUsername() + " §7enviou uma solicitação de amizade."));

                targetPlayer.sendMessage(Component.text("§7Use §f/amigo aceitar " + player.getUsername() + " §7ou §f/amigo negar " + player.getUsername()));
            }

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

    /*
     * =========================
     * ACCEPT
     * =========================
     */

    private void accept(VelocityCommandArgs args, Player player) {

        if (args.size() < 2) {

            args.sendMessage("§cUso: /amigo aceitar <jogador>");

            return;
        }

        Account target = findAccount(args.get(1));

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        try {

            friendManager.acceptRequest(player.getUniqueId(), target.getUniqueId());

            args.sendMessage("§aAgora você é amigo de §f" + target.getName() + "§a.");

            notifyPlayer(target.getUniqueId(), "§a§lAMIZADE §8» §f" + player.getUsername() + " §aaceitou sua solicitação de amizade.");

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

    /*
     * =========================
     * DENY
     * =========================
     */

    private void deny(VelocityCommandArgs args, Player player) {

        if (args.size() < 2) {

            args.sendMessage("§cUso: /amigo negar <jogador>");

            return;
        }

        Account target = findAccount(args.get(1));

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        try {

            friendManager.denyRequest(player.getUniqueId(), target.getUniqueId());

            args.sendMessage("§aSolicitação de §f" + target.getName() + " §arecusada.");

            notifyPlayer(target.getUniqueId(), "§c§lAMIZADE §8» §f" + player.getUsername() + " §7recusou sua solicitação de amizade.");

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

    /*
     * =========================
     * CANCEL
     * =========================
     */

    private void cancel(VelocityCommandArgs args, Player player) {

        if (args.size() < 2) {

            args.sendMessage("§cUso: /amigo cancelar <jogador>");

            return;
        }

        Account target = findAccount(args.get(1));

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        try {

            friendManager.cancelRequest(player.getUniqueId(), target.getUniqueId());

            args.sendMessage("§aSolicitação para §f" + target.getName() + " §acancelada.");

            notifyPlayer(target.getUniqueId(), "§7§lAMIZADE §8» §f" + player.getUsername() + " §7cancelou a solicitação de amizade.");

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

    /*
     * =========================
     * REMOVE
     * =========================
     */

    private void remove(VelocityCommandArgs args, Player player) {

        if (args.size() < 2) {

            args.sendMessage("§cUso: /amigo remover <jogador>");

            return;
        }

        Account target = findAccount(args.get(1));

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        try {

            friendManager.removeFriend(player.getUniqueId(), target.getUniqueId());

            args.sendMessage("§a§f" + target.getName() + " §afoi removido dos seus amigos.");

            notifyPlayer(target.getUniqueId(), "§c§lAMIZADE §8» §f" + player.getUsername() + " §7removeu você da lista de amigos.");

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

    /*
     * =========================
     * LIST
     * =========================
     */

    private void list(VelocityCommandArgs args, Player player) {

        Set<UUID> friends = friendManager.getFriends(player.getUniqueId());

        args.sendMessage("§8§m--------------------------------");

        args.sendMessage("§b§lAMIGOS §8» §f" + friends.size());

        args.sendMessage("");

        if (friends.isEmpty()) {

            args.sendMessage("§7Você ainda não possui amigos.");

            args.sendMessage("§7Use §f/amigo <jogador> §7para adicionar alguém.");

            args.sendMessage("§8§m--------------------------------");

            return;
        }

        List<UUID> sorted = new ArrayList<>(friends);

        sorted.sort(Comparator.comparing(this::getPlayerName, String.CASE_INSENSITIVE_ORDER));

        for (UUID uniqueId : sorted) {

            args.sendMessage("§7• §f" + getPlayerName(uniqueId));
        }

        args.sendMessage("");

        args.sendMessage("§8§m--------------------------------");
    }

    /*
     * =========================
     * REQUESTS
     * =========================
     */

    private void requests(VelocityCommandArgs args, Player player) {

        List<FriendRequest> requests = friendManager.getRequests(player.getUniqueId());

        args.sendMessage("§8§m--------------------------------");

        args.sendMessage("§e§lPEDIDOS §8» §f" + requests.size());

        args.sendMessage("");

        if (requests.isEmpty()) {

            args.sendMessage("§7Você não possui solicitações pendentes.");

            args.sendMessage("§8§m--------------------------------");

            return;
        }

        requests.sort(Comparator.comparing(FriendRequest::getCreatedAt));

        for (FriendRequest request : requests) {

            String name = getPlayerName(request.getSender());

            args.sendMessage("§e• §f" + name);

            args.sendMessage("§7  /amigo aceitar " + name);

            args.sendMessage("§7  /amigo negar " + name);
        }

        args.sendMessage("");

        args.sendMessage("§8§m--------------------------------");
    }

    /*
     * =========================
     * STATUS
     * =========================
     */

    private void status(VelocityCommandArgs args, Player player) {

        Set<UUID> friends = friendManager.getFriends(player.getUniqueId());

        args.sendMessage("§8§m--------------------------------");

        args.sendMessage("§b§lSTATUS DOS AMIGOS");

        args.sendMessage("");

        if (friends.isEmpty()) {

            args.sendMessage("§7Você não possui amigos.");

            args.sendMessage("§8§m--------------------------------");

            return;
        }

        List<UUID> sorted = new ArrayList<>(friends);

        sorted.sort(Comparator.comparing(this::getPlayerName, String.CASE_INSENSITIVE_ORDER));

        for (UUID uniqueId : sorted) {

            Player friend = proxyServer.getPlayer(uniqueId).orElse(null);

            if (friend == null) {

                args.sendMessage("§c● §f" + getPlayerName(uniqueId) + " §8» §7Offline");

                continue;
            }

            String server = friend.getCurrentServer().map(connection -> connection.getServer().getServerInfo().getName()).orElse("Conectando");

            args.sendMessage("§a● §f" + friend.getUsername() + " §8» §a" + server);
        }

        args.sendMessage("");

        args.sendMessage("§8§m--------------------------------");
    }

    /*
     * =========================
     * ACCOUNT
     * =========================
     */

    private Account findAccount(String name) {

        if (name == null || name.isBlank()) {

            return null;
        }

        Player player = proxyServer.getPlayer(name).orElse(null);

        if (player != null) {

            Account account = accountManager.get(player.getUniqueId());

            if (account != null) {
                return account;
            }
        }

        return accountManager.getByName(name);
    }

    private String getPlayerName(UUID uniqueId) {

        if (uniqueId == null) {
            return "Desconhecido";
        }

        Player player = proxyServer.getPlayer(uniqueId).orElse(null);

        if (player != null) {
            return player.getUsername();
        }

        Account account = accountManager.get(uniqueId);

        if (account != null && account.getName() != null && !account.getName().isBlank()) {

            return account.getName();
        }

        return uniqueId.toString();
    }

    private void notifyPlayer(UUID uniqueId, String message) {

        Player player = proxyServer.getPlayer(uniqueId).orElse(null);

        if (player != null) {
            player.sendMessage(Component.text(message));
        }
    }

    private List<String> onlinePlayers(String input) {

        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);

        return proxyServer.getAllPlayers().stream().map(Player::getUsername).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(normalized)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    private List<String> filter(List<String> values, String input) {

        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);

        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(normalized)).toList();
    }

    /*
     * =========================
     * HELP
     * =========================
     */

    private void sendHelp(VelocityCommandArgs args) {

        args.sendMessage("§8§m--------------------------------");

        args.sendMessage("§b§lAMIGOS");

        args.sendMessage("");

        args.sendMessage("§e/amigo <jogador> §7- Envia uma solicitação.");

        args.sendMessage("§e/amigo aceitar <jogador> §7- Aceita.");

        args.sendMessage("§e/amigo cancelar <jogador> §7- Cancela.");

        args.sendMessage("§e/amigo negar <jogador> §7- Recusa.");

        args.sendMessage("§e/amigo remover <jogador> §7- Remove.");

        args.sendMessage("§e/amigo lista §7- Lista seus amigos.");

        args.sendMessage("§e/amigo pedidos §7- Mostra solicitações.");

        args.sendMessage("§e/amigo status §7- Mostra o status.");

        args.sendMessage("§8§m--------------------------------");
    }
}