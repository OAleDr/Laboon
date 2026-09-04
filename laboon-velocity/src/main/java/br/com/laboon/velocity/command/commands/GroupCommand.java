package br.com.laboon.velocity.command.commands;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.TemporaryGroupService;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.account.group.GroupUpdatePublisher;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;
import br.com.laboon.core.profile.ProfileManager;
import br.com.laboon.velocity.command.VelocityCommandArgs;
import br.com.laboon.velocity.command.VelocityCommandSender;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class GroupCommand implements CommandClass {

    private final ProxyServer proxyServer;
    private final TemporaryGroupService temporaryGroupService;
    private final ProfileManager profileManager;
    private final GroupUpdatePublisher groupUpdatePublisher;

    public GroupCommand(ProxyServer proxyServer, TemporaryGroupService temporaryGroupService, ProfileManager profileManager, GroupUpdatePublisher groupUpdatePublisher) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (temporaryGroupService == null) {
            throw new IllegalArgumentException("TemporaryGroupService não pode ser nulo.");
        }

        if (profileManager == null) {
            throw new IllegalArgumentException("ProfileManager não pode ser nulo.");
        }

        if (groupUpdatePublisher == null) {
            throw new IllegalArgumentException("GroupUpdatePublisher não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.temporaryGroupService = temporaryGroupService;
        this.profileManager = profileManager;
        this.groupUpdatePublisher = groupUpdatePublisher;
    }

    @Override
    public Completer getCompleter() {

        return args -> {

            if (args.size() == 0) {

                return List.of("set", "remove", "get", "permanent");
            }

            if (args.size() == 1) {

                return filter(List.of("set", "remove", "get", "permanent"), args.get(0));
            }

            String subcommand = args.getSubcommand();

            if ("set".equals(subcommand) && args.size() == 2) {

                return completePlayers(args.getArgument(0));
            }

            if ("set".equals(subcommand) && args.size() == 3) {

                return completeGroups(args.getArgument(1));
            }

            if ("remove".equals(subcommand) && args.size() == 2) {

                return completePlayers(args.getArgument(0));
            }

            if ("remove".equals(subcommand) && args.size() == 3) {

                return completeGroups(args.getArgument(1));
            }

            if ("get".equals(subcommand) && args.size() == 2) {

                return completePlayers(args.getArgument(0));
            }

            if ("permanent".equals(subcommand) && args.size() == 2) {

                return completePlayers(args.getArgument(0));
            }

            if ("permanent".equals(subcommand) && args.size() == 3) {

                return completeGroups(args.getArgument(1));
            }

            return List.of();
        };
    }

    @Command(name = "group", aliases = {"grupo"}, description = "Gerencia grupos dos jogadores.", usage = "/group <set|remove|get|permanent>", subcommands = {"set", "remove", "get", "permanent"}, group = Group.ADMIN)
    public void execute(VelocityCommandArgs args) {

        VelocityCommandSender sender = args.getSender();

        if (args.isEmpty()) {
            sendUsage(sender);
            return;
        }

        switch (args.getSubcommand()) {

            case "set" -> handleSet(args);

            case "remove" -> handleRemove(args);

            case "get" -> handleGet(args);

            case "permanent" -> handlePermanent(args);

            default -> sendUsage(sender);
        }
    }

    private void handleSet(VelocityCommandArgs args) {

        VelocityCommandSender sender = args.getSender();

        if (args.size() < 4) {

            sender.sendMessage("§cUso: /group set <player> <group> <tempo>");

            return;
        }

        String playerName = args.getArgument(0);

        String groupName = args.getArgument(1);

        String durationValue = args.getArgument(2);

        Account account = findAccount(playerName);

        if (account == null) {

            sender.sendMessage("§cJogador não encontrado.");

            return;
        }

        Group group = parseGroup(groupName);

        if (group == null || group == Group.DEFAULT) {

            sender.sendMessage("§cGrupo inválido.");

            return;
        }

        Duration duration;

        try {

            duration = parseDuration(durationValue);

        } catch (IllegalArgumentException exception) {

            sender.sendMessage("§cTempo inválido. Use exemplos como §f30m§c, §f12h §cou §f7d§c.");

            return;
        }

        Instant previousExpiration = temporaryGroupService.getExpiration(account, group);

        temporaryGroupService.setTemporaryGroup(account, group, duration);

        /*
         * Informa todos os servidores Bukkit
         * sobre a alteração.
         */
        groupUpdatePublisher.publish(account);

        Instant expiration = temporaryGroupService.getExpiration(account, group);

        sender.sendMessage("§aGrupo temporário §f" + group.getDisplayName() + " §aadicionado para §f" + account.getName() + "§a.");

        if (previousExpiration != null) {

            sender.sendMessage("§7Tempo acumulado: §f" + formatDuration(duration));

        } else {

            sender.sendMessage("§7Duração: §f" + formatDuration(duration));
        }

        sender.sendMessage("§7Expira em: §f" + formatExpiration(expiration));
    }

    private void handleRemove(VelocityCommandArgs args) {

        VelocityCommandSender sender = args.getSender();

        if (args.size() < 3) {

            sender.sendMessage("§cUso: /group remove <player> <group>");

            return;
        }

        String playerName = args.getArgument(0);

        String groupName = args.getArgument(1);

        Account account = findAccount(playerName);

        if (account == null) {

            sender.sendMessage("§cJogador não encontrado.");

            return;
        }

        Group group = parseGroup(groupName);

        if (group == null || group == Group.DEFAULT) {

            sender.sendMessage("§cGrupo inválido.");

            return;
        }

        boolean removed = temporaryGroupService.removeTemporaryGroup(account, group);

        if (!removed) {

            sender.sendMessage("§cO jogador não possui esse grupo temporário.");

            return;
        }

        /*
         * Informa todos os servidores Bukkit
         * sobre a remoção.
         */
        groupUpdatePublisher.publish(account);

        sender.sendMessage("§aGrupo temporário §f" + group.getDisplayName() + " §aremovido de §f" + account.getName() + "§a.");
    }

    private void handlePermanent(VelocityCommandArgs args) {

        VelocityCommandSender sender = args.getSender();

        if (args.size() < 3) {

            sender.sendMessage("§cUso: /group permanent <player> <group>");

            return;
        }

        String playerName = args.getArgument(0);

        String groupName = args.getArgument(1);

        Account account = findAccount(playerName);

        if (account == null) {

            sender.sendMessage("§cJogador não encontrado.");

            return;
        }

        Group group = parseGroup(groupName);

        if (group == null) {

            sender.sendMessage("§cGrupo inválido.");

            return;
        }

        Group previousGroup = account.getGroup();

        account.setGroup(group);

        /*
         * A tag acompanha o grupo permanente.
         */
        account.setTag(group.getAbbreviation());

        temporaryGroupService.getAccountRepository().save(account);

        /*
         * Informa todos os servidores Bukkit
         * sobre a alteração.
         */
        groupUpdatePublisher.publish(account);

        sender.sendMessage("§aGrupo permanente de §f" + account.getName() + " §aalterado.");

        sender.sendMessage("§7Anterior: §f" + formatGroup(previousGroup));

        sender.sendMessage("§7Atual: §f" + formatGroup(group));
    }

    private void handleGet(VelocityCommandArgs args) {

        VelocityCommandSender sender = args.getSender();

        if (args.size() < 2) {

            sender.sendMessage("§cUso: /group get <player>");

            return;
        }

        String playerName = args.getArgument(0);

        Account account = findAccount(playerName);

        if (account == null) {

            sender.sendMessage("§cJogador não encontrado.");

            return;
        }

        Group effectiveGroup = getEffectiveGroup(account);

        sender.sendMessage("§8§m--------------------------------");

        sender.sendMessage("§b§lGRUPOS §8» §f" + account.getName());

        sender.sendMessage("");

        sender.sendMessage("§7Grupo permanente: §f" + formatGroup(account.getGroup()));

        sender.sendMessage("§7Grupo atual: §f" + formatGroup(effectiveGroup));

        sender.sendMessage("");

        sender.sendMessage("§7Grupos temporários:");

        List<String> groups = new ArrayList<>();

        Instant now = Instant.now();

        account.getTemporaryGroups().entrySet().stream().filter(entry -> entry.getValue() != null && entry.getValue().isAfter(now)).sorted(Comparator.comparing(entry -> entry.getKey().getPower(), Comparator.reverseOrder())).forEach(entry -> {

            Group group = entry.getKey();

            Instant expiration = entry.getValue();

            groups.add("§8• §f" + formatGroup(group) + " §8→ §f" + formatExpiration(expiration));
        });

        if (groups.isEmpty()) {

            sender.sendMessage("§8• §7Nenhum grupo temporário.");

        } else {

            for (String group : groups) {

                sender.sendMessage(group);
            }
        }

        sender.sendMessage("");

        sender.sendMessage("§8§m--------------------------------");
    }

    private Account findAccount(String name) {

        if (name == null || name.isBlank()) {

            return null;
        }

        Player player = proxyServer.getPlayer(name).orElse(null);

        if (player != null) {

            var profile = profileManager.get(player.getUniqueId());

            if (profile != null) {

                return profile.getAccount();
            }
        }

        return temporaryGroupService.getAccountRepository().findByName(name);
    }

    private Group getEffectiveGroup(Account account) {

        Group effectiveGroup = account.getGroup();

        Instant now = Instant.now();

        for (var entry : account.getTemporaryGroups().entrySet()) {

            Group temporaryGroup = entry.getKey();

            Instant expiration = entry.getValue();

            if (temporaryGroup == null || expiration == null) {

                continue;
            }

            if (!expiration.isAfter(now)) {
                continue;
            }

            if (temporaryGroup.getPower() > effectiveGroup.getPower()) {

                effectiveGroup = temporaryGroup;
            }
        }

        return effectiveGroup;
    }

    private Group parseGroup(String value) {

        if (value == null || value.isBlank()) {

            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        for (Group group : Group.values()) {

            if (group.name().equalsIgnoreCase(normalized)) {

                return group;
            }

            if (group.getAbbreviation().equalsIgnoreCase(value.trim())) {

                return group;
            }
        }

        return null;
    }

    private Duration parseDuration(String value) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException();
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        if (normalized.length() < 2) {
            throw new IllegalArgumentException();
        }

        String number = normalized.substring(0, normalized.length() - 1);

        char unit = normalized.charAt(normalized.length() - 1);

        long amount;

        try {

            amount = Long.parseLong(number);

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException();
        }

        if (amount <= 0) {
            throw new IllegalArgumentException();
        }

        return switch (unit) {

            case 's' -> Duration.ofSeconds(amount);

            case 'm' -> Duration.ofMinutes(amount);

            case 'h' -> Duration.ofHours(amount);

            case 'd' -> Duration.ofDays(amount);

            default -> throw new IllegalArgumentException();
        };
    }

    private String formatExpiration(Instant expiration) {

        if (expiration == null) {
            return "desconhecida";
        }

        Duration remaining = Duration.between(Instant.now(), expiration);

        if (remaining.isZero() || remaining.isNegative()) {

            return "expirado";
        }

        return formatDuration(remaining);
    }

    private String formatDuration(Duration duration) {

        if (duration == null || duration.isZero() || duration.isNegative()) {

            return "0s";
        }

        long seconds = duration.getSeconds();

        long days = seconds / 86_400;

        seconds %= 86_400;

        long hours = seconds / 3_600;

        seconds %= 3_600;

        long minutes = seconds / 60;

        seconds %= 60;

        if (days > 0) {

            if (hours > 0) {

                return days + "d " + hours + "h";
            }

            return days + "d";
        }

        if (hours > 0) {

            if (minutes > 0) {

                return hours + "h " + minutes + "min";
            }

            return hours + "h";
        }

        if (minutes > 0) {

            if (seconds > 0) {

                return minutes + "min " + seconds + "s";
            }

            return minutes + "min";
        }

        return seconds + "s";
    }

    private String formatGroup(Group group) {

        if (group == null || group == Group.DEFAULT) {

            return "DEFAULT";
        }

        return group.getDisplayName();
    }

    private List<String> completePlayers(String input) {

        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);

        return proxyServer.getAllPlayers().stream().map(Player::getUsername).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(normalized)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    private List<String> completeGroups(String input) {

        List<String> groups = Arrays.stream(Group.values()).filter(group -> group != Group.DEFAULT).map(group -> group.name().toLowerCase(Locale.ROOT)).toList();

        return filter(groups, input);
    }

    private List<String> filter(List<String> values, String input) {

        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);

        return values.stream().filter(value -> value.startsWith(normalized)).sorted().toList();
    }

    private void sendUsage(VelocityCommandSender sender) {

        sender.sendMessage("§b§lLaboon §8» §fComandos de grupo:");

        sender.sendMessage("§7/group set <player> <group> <tempo>");

        sender.sendMessage("§7/group remove <player> <group>");

        sender.sendMessage("§7/group get <player>");

        sender.sendMessage("§7/group permanent <player> <group>");
    }
}