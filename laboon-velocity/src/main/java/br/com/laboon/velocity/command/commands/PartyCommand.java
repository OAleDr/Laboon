package br.com.laboon.velocity.command.commands;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;
import br.com.laboon.core.party.Party;
import br.com.laboon.core.party.PartyInvite;
import br.com.laboon.core.party.PartyManager;
import br.com.laboon.core.server.ServerType;
import br.com.laboon.velocity.command.VelocityCommandArgs;
import br.com.laboon.velocity.party.PartyServerService;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Locale;

public final class PartyCommand implements CommandClass {

    private final ProxyServer proxyServer;
    private final PartyManager partyManager;
    private final AccountManager accountManager;
    private final PartyServerService partyServerService;

    public PartyCommand(ProxyServer proxyServer, PartyManager partyManager, AccountManager accountManager, PartyServerService partyServerService) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (partyManager == null) {
            throw new IllegalArgumentException("PartyManager não pode ser nulo.");
        }

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        if (partyServerService == null) {
            throw new IllegalArgumentException("PartyServerService não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.partyManager = partyManager;
        this.accountManager = accountManager;
        this.partyServerService = partyServerService;
    }

    /*
     * =========================
     * COMPLETER
     * =========================
     */

    @Override
    public Completer getCompleter() {

        return args -> {

            List<String> commands = List.of("create", "invite", "accept", "deny", "leave", "kick", "disband", "info", "promote", "bedwars", "skywars");

            if (args.size() == 0) {
                return commands;
            }

            if (args.size() == 1) {

                String input = args.get(0) == null ? "" : args.get(0).toLowerCase(Locale.ROOT);

                return commands.stream().filter(command -> command.startsWith(input)).toList();
            }

            if (args.size() == 2) {

                String subcommand = args.get(0);

                if (subcommand == null) {
                    return List.of();
                }

                if (subcommand.equalsIgnoreCase("invite") || subcommand.equalsIgnoreCase("kick") || subcommand.equalsIgnoreCase("promote")) {

                    String input = args.get(1) == null ? "" : args.get(1).toLowerCase(Locale.ROOT);

                    return proxyServer.getAllPlayers().stream().map(Player::getUsername).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input)).toList();
                }
            }

            return List.of();
        };
    }

    /*
     * =========================
     * COMMAND
     * =========================
     */

    @Command(name = "party", aliases = {"p"}, description = "Gerencia sua Party.", usage = "/party <create|invite|accept|deny|leave|kick|disband|info|promote|bedwars|skywars>", subcommands = {"create", "invite", "accept", "deny", "leave", "kick", "disband", "info", "promote", "bedwars", "skywars"})
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

        String subcommand = args.get(0);

        if (subcommand == null || subcommand.isBlank()) {

            sendHelp(args);

            return;
        }

        switch (subcommand.toLowerCase(Locale.ROOT)) {

            case "create" -> create(args, player);

            case "invite" -> invite(args, player);

            case "accept" -> accept(args, player);

            case "deny" -> deny(args, player);

            case "leave" -> leave(args, player);

            case "kick" -> kick(args, player);

            case "disband" -> disband(args, player);

            case "info" -> info(args, player);

            case "promote" -> promote(args, player);

            case "bedwars" -> bedwars(args, player);

            case "skywars" -> skywars(args, player);

            default -> sendHelp(args);
        }
    }

    /*
     * =========================
     * CREATE
     * =========================
     */

    private void create(VelocityCommandArgs args, Player player) {

        if (partyManager.hasParty(player.getUniqueId())) {

            args.sendMessage("§cVocê já está em uma Party.");

            return;
        }

        try {

            Party party = partyManager.create(player.getUniqueId());

            args.sendMessage("§aParty criada com sucesso!");

            args.sendMessage("§7Use §f/party invite <jogador> §7para convidar seus amigos.");

        } catch (Exception exception) {

            args.sendMessage("§cNão foi possível criar a Party.");
        }
    }

    /*
     * =========================
     * INVITE
     * =========================
     */

    private void invite(VelocityCommandArgs args, Player player) {

        if (args.size() < 2) {

            args.sendMessage("§cUso: /party invite <jogador>");

            return;
        }

        Party party = partyManager.getByMember(player.getUniqueId());

        if (party == null) {

            args.sendMessage("§cVocê não está em uma Party.");

            return;
        }

        if (!party.isLeader(player.getUniqueId())) {

            args.sendMessage("§cSomente o líder pode convidar jogadores.");

            return;
        }

        Player target = findPlayer(args.get(1));

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {

            args.sendMessage("§cVocê não pode convidar a si mesmo.");

            return;
        }

        try {

            PartyInvite invite = partyManager.createInvite(party.getUniqueId(), player.getUniqueId(), target.getUniqueId());

            args.sendMessage("§aConvite enviado para §f" + target.getUsername() + "§a.");

            target.sendMessage(Component.text("§eVocê recebeu um convite para entrar na Party de " + player.getUsername() + "."));

            target.sendMessage(Component.text("§7Use §f/party accept §7para aceitar."));

            target.sendMessage(Component.text("§7O convite expira em §f60 segundos§7."));

        } catch (IllegalStateException exception) {

            args.sendMessage("§c" + exception.getMessage());

        } catch (Exception exception) {

            args.sendMessage("§cNão foi possível enviar o convite.");
        }
    }

    /*
     * =========================
     * ACCEPT
     * =========================
     */

    private void accept(VelocityCommandArgs args, Player player) {

        if (partyManager.hasParty(player.getUniqueId())) {

            args.sendMessage("§cVocê já está em uma Party.");

            return;
        }

        PartyInvite invite = partyManager.getInvite(player.getUniqueId());

        if (invite == null) {

            args.sendMessage("§cVocê não possui nenhum convite pendente.");

            return;
        }

        String inviterName = getPlayerName(invite.getInviter());

        if (partyManager.acceptInvite(player.getUniqueId())) {

            Party party = partyManager.getByMember(player.getUniqueId());

            args.sendMessage("§aVocê entrou na Party de §f" + inviterName + "§a.");

            if (party != null) {

                notifyParty(party, "§a" + player.getUsername() + " §aentrou na Party.");
            }

        } else {

            args.sendMessage("§cNão foi possível aceitar o convite.");
        }
    }

    /*
     * =========================
     * DENY
     * =========================
     */

    private void deny(VelocityCommandArgs args, Player player) {

        if (!partyManager.hasInvite(player.getUniqueId())) {

            args.sendMessage("§cVocê não possui nenhum convite pendente.");

            return;
        }

        if (partyManager.declineInvite(player.getUniqueId())) {

            args.sendMessage("§aConvite recusado.");

        } else {

            args.sendMessage("§cNão foi possível recusar o convite.");
        }
    }

    /*
     * =========================
     * LEAVE
     * =========================
     */

    private void leave(VelocityCommandArgs args, Player player) {

        Party party = partyManager.getByMember(player.getUniqueId());

        if (party == null) {

            args.sendMessage("§cVocê não está em uma Party.");

            return;
        }

        if (party.isLeader(player.getUniqueId())) {

            args.sendMessage("§cO líder não pode sair da Party.");

            args.sendMessage("§7Use §f/party promote <jogador> §7ou §f/party disband§7.");

            return;
        }

        if (partyManager.removeMember(party.getUniqueId(), player.getUniqueId())) {

            args.sendMessage("§aVocê saiu da Party.");

            notifyParty(party, "§e" + player.getUsername() + " §esaiu da Party.");

        } else {

            args.sendMessage("§cNão foi possível sair da Party.");
        }
    }

    /*
     * =========================
     * KICK
     * =========================
     */

    private void kick(VelocityCommandArgs args, Player player) {

        if (args.size() < 2) {

            args.sendMessage("§cUso: /party kick <jogador>");

            return;
        }

        Party party = partyManager.getByMember(player.getUniqueId());

        if (party == null) {

            args.sendMessage("§cVocê não está em uma Party.");

            return;
        }

        if (!party.isLeader(player.getUniqueId())) {

            args.sendMessage("§cSomente o líder pode expulsar jogadores.");

            return;
        }

        Player target = findPlayer(args.get(1));

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        if (!party.containsMember(target.getUniqueId())) {

            args.sendMessage("§cEsse jogador não está na sua Party.");

            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {

            args.sendMessage("§cVocê não pode expulsar a si mesmo.");

            return;
        }

        if (partyManager.removeMember(party.getUniqueId(), target.getUniqueId())) {

            args.sendMessage("§aJogador §f" + target.getUsername() + " §aremovido da Party.");

            target.sendMessage(Component.text("§cVocê foi removido da Party."));

            notifyParty(party, "§e" + target.getUsername() + " §efoi removido da Party.");

        } else {

            args.sendMessage("§cNão foi possível remover o jogador.");
        }
    }

    /*
     * =========================
     * DISBAND
     * =========================
     */

    private void disband(VelocityCommandArgs args, Player player) {

        Party party = partyManager.getByMember(player.getUniqueId());

        if (party == null) {

            args.sendMessage("§cVocê não está em uma Party.");

            return;
        }

        if (!party.isLeader(player.getUniqueId())) {

            args.sendMessage("§cSomente o líder pode desfazer a Party.");

            return;
        }

        notifyParty(party, "§cA Party foi desfeita pelo líder.");

        partyManager.disband(party.getUniqueId());
    }

    /*
     * =========================
     * INFO
     * =========================
     */

    private void info(VelocityCommandArgs args, Player player) {

        Party party = partyManager.getByMember(player.getUniqueId());

        if (party == null) {

            args.sendMessage("§cVocê não está em uma Party.");

            return;
        }

        args.sendMessage("§8§m-----------------------------");

        args.sendMessage("§e§lPARTY");

        args.sendMessage("§7Líder: §f" + getPlayerName(party.getLeader()));

        args.sendMessage("§7Membros: §f" + party.getMemberCount());

        args.sendMessage("");

        for (var member : party.getMembers()) {

            String name = getPlayerName(member.getUniqueId());

            if (party.isLeader(member.getUniqueId())) {

                args.sendMessage("§e★ §f" + name);

            } else {

                args.sendMessage("§7• §f" + name);
            }
        }

        args.sendMessage("§8§m-----------------------------");
    }

    /*
     * =========================
     * PROMOTE
     * =========================
     */

    private void promote(VelocityCommandArgs args, Player player) {

        if (args.size() < 2) {

            args.sendMessage("§cUso: /party promote <jogador>");

            return;
        }

        Party party = partyManager.getByMember(player.getUniqueId());

        if (party == null) {

            args.sendMessage("§cVocê não está em uma Party.");

            return;
        }

        if (!party.isLeader(player.getUniqueId())) {

            args.sendMessage("§cSomente o líder pode promover outro jogador.");

            return;
        }

        Player target = findPlayer(args.get(1));

        if (target == null) {

            args.sendMessage("§cJogador não encontrado.");

            return;
        }

        if (!party.containsMember(target.getUniqueId())) {

            args.sendMessage("§cEsse jogador não está na Party.");

            return;
        }

        if (party.isLeader(target.getUniqueId())) {

            args.sendMessage("§cEsse jogador já é o líder.");

            return;
        }

        if (partyManager.transferLeader(party.getUniqueId(), target.getUniqueId())) {

            notifyParty(party, "§e" + target.getUsername() + " §eagora é o novo líder da Party.");

        } else {

            args.sendMessage("§cNão foi possível transferir a liderança.");
        }
    }

    /*
     * =========================
     * BEDWARS
     * =========================
     */

    private void bedwars(VelocityCommandArgs args, Player player) {

        partyServerService.sendPartyTo(player.getUniqueId(), ServerType.BEDWARS);
    }

    /*
     * =========================
     * SKYWARS
     * =========================
     */

    private void skywars(VelocityCommandArgs args, Player player) {

        partyServerService.sendPartyTo(player.getUniqueId(), ServerType.SKYWARS);
    }

    /*
     * =========================
     * PLAYER
     * =========================
     */

    private Player findPlayer(String name) {

        if (name == null || name.isBlank()) {

            return null;
        }

        return proxyServer.getPlayer(name).orElse(null);
    }

    /*
     * =========================
     * PLAYER NAME
     * =========================
     */

    private String getPlayerName(java.util.UUID uniqueId) {

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

    /*
     * =========================
     * NOTIFY PARTY
     * =========================
     */

    private void notifyParty(Party party, String message) {

        if (party == null) {
            return;
        }

        for (var member : party.getMembers()) {

            Player player = proxyServer.getPlayer(member.getUniqueId()).orElse(null);

            if (player == null) {
                continue;
            }

            player.sendMessage(Component.text(message));
        }
    }

    /*
     * =========================
     * HELP
     * =========================
     */

    private void sendHelp(VelocityCommandArgs args) {

        args.sendMessage("§8§m-----------------------------");

        args.sendMessage("§e§lPARTY");

        args.sendMessage("§e/party create §7- Cria uma Party.");

        args.sendMessage("§e/party invite <jogador> §7- Convida um jogador.");

        args.sendMessage("§e/party accept §7- Aceita um convite.");

        args.sendMessage("§e/party deny §7- Recusa um convite.");

        args.sendMessage("§e/party leave §7- Sai da Party.");

        args.sendMessage("§e/party kick <jogador> §7- Expulsa um jogador.");

        args.sendMessage("§e/party promote <jogador> §7- Transfere a liderança.");

        args.sendMessage("§e/party info §7- Mostra informações da Party.");

        args.sendMessage("§e/party disband §7- Desfaz a Party.");

        args.sendMessage("");

        args.sendMessage("§a/party bedwars §7- Entra no BedWars com a Party.");

        args.sendMessage("§b/party skywars §7- Entra no SkyWars com a Party.");

        args.sendMessage("§8§m-----------------------------");
    }
}