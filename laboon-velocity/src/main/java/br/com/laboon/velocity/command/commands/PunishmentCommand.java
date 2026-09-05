package br.com.laboon.velocity.command.commands;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.account.punishment.Ban;
import br.com.laboon.core.account.punishment.Mute;
import br.com.laboon.core.account.punishment.PunishmentDurationParser;
import br.com.laboon.core.account.punishment.PunishmentService;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;
import br.com.laboon.velocity.command.VelocityCommandArgs;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class PunishmentCommand implements CommandClass {

    private final ProxyServer proxyServer;
    private final AccountManager accountManager;
    private final PunishmentService punishmentService;

    public PunishmentCommand(ProxyServer proxyServer, AccountManager accountManager, PunishmentService punishmentService) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        if (punishmentService == null) {
            throw new IllegalArgumentException("PunishmentService não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.accountManager = accountManager;
        this.punishmentService = punishmentService;
    }

    @Override
    public Completer getCompleter() {

        return args -> {

            if (args.size() == 1) {

                String input = args.get(0) == null ? "" : args.get(0).toLowerCase(Locale.ROOT);

                return proxyServer.getAllPlayers().stream().map(Player::getUsername).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input)).toList();
            }

            return List.of();
        };
    }

    @Command(name = "ban", description = "Bane um jogador permanentemente.", usage = "/ban <jogador> <motivo>", group = Group.MOD)
    public void ban(VelocityCommandArgs args) {

        executeBan(args, false);
    }

    @Command(name = "tempban", description = "Bane um jogador temporariamente.", usage = "/tempban <jogador> <tempo> <motivo>", group = Group.MOD)
    public void tempBan(VelocityCommandArgs args) {

        executeBan(args, true);
    }

    @Command(name = "unban", description = "Remove o ban de um jogador.", usage = "/unban <jogador>", group = Group.MOD)
    public void unban(VelocityCommandArgs args) {

        if (args.size() != 1) {
            args.sendMessage("§cUso: /unban <jogador>");
            return;
        }

        Player staff = args.getPlayer();

        if (staff == null) {
            args.sendMessage("§cApenas jogadores podem utilizar este comando.");
            return;
        }

        Account target = findAccount(args.get(0));

        if (target == null) {
            args.sendMessage("§cJogador não encontrado.");
            return;
        }

        try {

            Ban ban = punishmentService.unban(target, staff.getUsername(), staff.getUniqueId());

            args.sendMessage("§aBan de §f" + target.getName() + " §aremovido com sucesso.");

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

    @Command(name = "mute", description = "Muta um jogador permanentemente.", usage = "/mute <jogador> <motivo>", group = Group.MOD)
    public void mute(VelocityCommandArgs args) {

        executeMute(args, false);
    }

    @Command(name = "tempmute", description = "Muta um jogador temporariamente.", usage = "/tempmute <jogador> <tempo> <motivo>", group = Group.MOD)
    public void tempMute(VelocityCommandArgs args) {

        executeMute(args, true);
    }

    @Command(name = "unmute", description = "Remove o mute de um jogador.", usage = "/unmute <jogador>", group = Group.MOD)
    public void unmute(VelocityCommandArgs args) {

        if (args.size() != 1) {
            args.sendMessage("§cUso: /unmute <jogador>");
            return;
        }

        Player staff = args.getPlayer();

        if (staff == null) {
            args.sendMessage("§cApenas jogadores podem utilizar este comando.");
            return;
        }

        Account target = findAccount(args.get(0));

        if (target == null) {
            args.sendMessage("§cJogador não encontrado.");
            return;
        }

        try {

            punishmentService.unmute(target, staff.getUsername(), staff.getUniqueId());

            args.sendMessage("§aMute de §f" + target.getName() + " §aremovido com sucesso.");

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

    @Command(name = "kick", description = "Expulsa um jogador.", usage = "/kick <jogador> <motivo>", group = Group.HELPER)
    public void kick(VelocityCommandArgs args) {

        if (args.size() < 2) {
            args.sendMessage("§cUso: /kick <jogador> <motivo>");
            return;
        }

        Player staff = args.getPlayer();

        if (staff == null) {
            args.sendMessage("§cApenas jogadores podem utilizar este comando.");
            return;
        }

        Account target = findAccount(args.get(0));

        if (target == null) {
            args.sendMessage("§cJogador não encontrado.");
            return;
        }

        String reason = join(args, 1);

        Player targetPlayer = proxyServer.getPlayer(target.getUniqueId()).orElse(null);

        String server = targetPlayer != null && targetPlayer.getCurrentServer().isPresent() ? targetPlayer.getCurrentServer().get().getServerInfo().getName() : null;

        punishmentService.kick(target, staff.getUsername(), staff.getUniqueId(), server, reason);

        if (targetPlayer != null) {

            targetPlayer.disconnect(net.kyori.adventure.text.Component.text("Você foi expulso do servidor.\n\n" + "Motivo: " + reason));
        }

        args.sendMessage("§aJogador §f" + target.getName() + " §aexpulso com sucesso.");
    }

    private void executeBan(VelocityCommandArgs args, boolean temporary) {

        int minimum = temporary ? 3 : 2;

        if (args.size() < minimum) {

            if (temporary) {
                args.sendMessage("§cUso: /tempban <jogador> <tempo> <motivo>");
            } else {
                args.sendMessage("§cUso: /ban <jogador> <motivo>");
            }

            return;
        }

        Player staff = args.getPlayer();

        if (staff == null) {
            args.sendMessage("§cApenas jogadores podem utilizar este comando.");
            return;
        }

        Account target = findAccount(args.get(0));

        if (target == null) {
            args.sendMessage("§cJogador não encontrado.");
            return;
        }

        if (target.getUniqueId().equals(staff.getUniqueId())) {
            args.sendMessage("§cVocê não pode se punir.");
            return;
        }

        Duration duration = null;
        int reasonIndex;

        if (temporary) {

            try {
                duration = PunishmentDurationParser.parse(args.get(1));
            } catch (IllegalArgumentException exception) {

                args.sendMessage("§cTempo inválido. Use, por exemplo: 30m, 2h ou 7d.");
                return;
            }

            reasonIndex = 2;

        } else {

            reasonIndex = 1;
        }

        String reason = join(args, reasonIndex);

        if (reason.isBlank()) {
            args.sendMessage("§cInforme um motivo.");
            return;
        }

        Player targetPlayer = proxyServer.getPlayer(target.getUniqueId()).orElse(null);

        String ip = null;
        String server = null;

        if (targetPlayer != null) {

            ip = targetPlayer.getRemoteAddress().getAddress().getHostAddress();

            server = targetPlayer.getCurrentServer().map(connection -> connection.getServerInfo().getName()).orElse(null);
        }

        try {

            Ban ban;

            if (temporary) {

                ban = punishmentService.tempBan(target, staff.getUsername(), staff.getUniqueId(), ip, server, reason, duration);

            } else {

                ban = punishmentService.ban(target, staff.getUsername(), staff.getUniqueId(), ip, server, reason);
            }

            if (targetPlayer != null) {

                targetPlayer.disconnect(net.kyori.adventure.text.Component.text(buildBanMessage(ban)));
            }

            args.sendMessage("§aJogador §f" + target.getName() + " §abanido com sucesso.");

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

    private void executeMute(VelocityCommandArgs args, boolean temporary) {

        int minimum = temporary ? 3 : 2;

        if (args.size() < minimum) {

            if (temporary) {
                args.sendMessage("§cUso: /tempmute <jogador> <tempo> <motivo>");
            } else {
                args.sendMessage("§cUso: /mute <jogador> <motivo>");
            }

            return;
        }

        Player staff = args.getPlayer();

        if (staff == null) {
            args.sendMessage("§cApenas jogadores podem utilizar este comando.");
            return;
        }

        Account target = findAccount(args.get(0));

        if (target == null) {
            args.sendMessage("§cJogador não encontrado.");
            return;
        }

        if (target.getUniqueId().equals(staff.getUniqueId())) {
            args.sendMessage("§cVocê não pode se punir.");
            return;
        }

        Duration duration = null;
        int reasonIndex;

        if (temporary) {

            try {

                duration = PunishmentDurationParser.parse(args.get(1));

            } catch (IllegalArgumentException exception) {

                args.sendMessage("§cTempo inválido. Use, por exemplo: 30m, 2h ou 7d.");

                return;
            }

            reasonIndex = 2;

        } else {

            reasonIndex = 1;
        }

        String reason = join(args, reasonIndex);

        Player targetPlayer = proxyServer.getPlayer(target.getUniqueId()).orElse(null);

        String ip = null;
        String server = null;

        if (targetPlayer != null) {

            ip = targetPlayer.getRemoteAddress().getAddress().getHostAddress();

            server = targetPlayer.getCurrentServer().map(connection -> connection.getServerInfo().getName()).orElse(null);
        }

        try {

            Mute mute;

            if (temporary) {

                mute = punishmentService.tempMute(target, staff.getUsername(), staff.getUniqueId(), ip, server, reason, duration);

            } else {

                mute = punishmentService.mute(target, staff.getUsername(), staff.getUniqueId(), ip, server, reason);
            }

            args.sendMessage("§aJogador §f" + target.getName() + " §amutado com sucesso.");

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());
        }
    }

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

    private String join(VelocityCommandArgs args, int start) {

        if (start >= args.size()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = start; i < args.size(); i++) {

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(args.get(i));
        }

        return result.toString().trim();
    }

    private String buildBanMessage(Ban ban) {

        String expiration = ban.isPermanent() ? "Permanente" : ban.getExpire().toString();

        return "Você foi banido da rede Laboon.\n\n" + "Motivo: " + ban.getReason() + "\n" + "Duração: " + expiration;
    }
}