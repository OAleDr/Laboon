package br.com.laboon.bukkit.command.commands;

import br.com.laboon.bukkit.command.BukkitCommandArgs;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;
import br.com.laboon.core.profile.PlayerProfile;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public final class CoinTestCommand implements CommandClass {

    private final ProfileProvider profileProvider;

    public CoinTestCommand(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    @Override
    public Completer getCompleter() {
        return args -> {

            if (args.size() < 2) {
                return List.of();
            }

            String subcommand = args.getSubcommand();

            if (!"get".equals(subcommand) && !"add".equals(subcommand) && !"remove".equals(subcommand)) {

                return List.of();
            }

            String input = args.getArgument(0);

            if (input == null) {
                input = "";
            }

            String search = input.toLowerCase();

            return List.of("bedwars", "skywars").stream().filter(game -> game.startsWith(search)).collect(Collectors.toList());
        };
    }

    @Command(name = "coinstest", description = "Testa o sistema de coins.", usage = "/coinstest <get|add|remove> <jogo> [quantidade]", subcommands = {"get", "add", "remove"}, group = Group.ADMIN)
    public void execute(BukkitCommandArgs args) {

        if (!args.isPlayer()) {
            args.getSender().sendMessage(ChatColor.RED + "Apenas jogadores podem usar este comando.");
            return;
        }

        PlayerProfile profile = profileProvider.getProfile(args.getSender().getPlayer());

        if (profile == null) {
            args.getSender().sendMessage(ChatColor.RED + "Seu perfil não foi carregado.");
            return;
        }

        if (args.size() < 2) {
            sendUsage(args);
            return;
        }

        String action = args.getSubcommand();

        String game = args.getArgument(0).toLowerCase();

        switch (action) {

            case "get":

                long coins = profile.getCoins(game);

                args.getSender().sendMessage(ChatColor.GREEN + "Coins de " + game + ": " + coins);

                break;

            case "add":

                if (args.size() < 3) {
                    args.getSender().sendMessage(ChatColor.RED + "Informe a quantidade.");
                    return;
                }

                long amount;

                try {

                    amount = Long.parseLong(args.getArgument(1));

                } catch (NumberFormatException exception) {

                    args.getSender().sendMessage(ChatColor.RED + "Quantidade inválida.");

                    return;
                }

                if (amount <= 0) {
                    args.getSender().sendMessage(ChatColor.RED + "A quantidade deve ser maior que zero.");
                    return;
                }

                profile.addCoins(game, amount);

                args.getSender().sendMessage(ChatColor.GREEN + "Adicionado " + amount + " coins em " + game + ".");

                break;

            case "remove":

                if (args.size() < 3) {
                    args.getSender().sendMessage(ChatColor.RED + "Informe a quantidade.");
                    return;
                }

                long removeAmount;

                try {

                    removeAmount = Long.parseLong(args.getArgument(1));

                } catch (NumberFormatException exception) {

                    args.getSender().sendMessage(ChatColor.RED + "Quantidade inválida.");

                    return;
                }

                if (removeAmount <= 0) {
                    args.getSender().sendMessage(ChatColor.RED + "A quantidade deve ser maior que zero.");
                    return;
                }

                profile.removeCoins(game, removeAmount);

                args.getSender().sendMessage(ChatColor.GREEN + "Removido " + removeAmount + " coins de " + game + ".");

                break;

            default:

                sendUsage(args);
        }
    }

    private void sendUsage(BukkitCommandArgs args) {
        args.getSender().sendMessage(ChatColor.YELLOW + "Uso: /coinstest <get|add|remove> <jogo> [quantidade]");
    }
}