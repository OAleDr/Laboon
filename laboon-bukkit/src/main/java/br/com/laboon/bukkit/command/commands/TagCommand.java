package br.com.laboon.bukkit.command.commands;

import br.com.laboon.bukkit.command.BukkitCommandArgs;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.Completer;
import br.com.laboon.core.profile.PlayerProfile;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TagCommand implements CommandClass {

    private final ProfileProvider profileProvider;

    public TagCommand(ProfileProvider profileProvider) {

        if (profileProvider == null) {
            throw new IllegalArgumentException("ProfileProvider não pode ser nulo.");
        }

        this.profileProvider = profileProvider;
    }

    /**
     * /tag
     * /tag <tag>
     * /tag off
     */
    @Command(name = "tag", aliases = {"tags"}, description = "Escolhe sua tag.", usage = "/tag <tag|off>")
    public void execute(BukkitCommandArgs args) {

        /*
         * O comando só pode ser usado
         * por jogadores.
         */
        if (!args.isPlayer()) {

            args.sendMessage(ChatColor.RED + "Apenas jogadores podem usar este comando.");

            return;
        }

        PlayerProfile profile = profileProvider.getProfile(args.getSender().getPlayer());

        /*
         * Perfil ainda não carregado.
         */
        if (profile == null) {

            args.sendMessage(ChatColor.RED + "Seu perfil ainda não foi carregado.");

            return;
        }

        /*
         * /tag
         *
         * Mostra as tags disponíveis.
         */
        if (args.size() == 0) {

            sendAvailableTags(args, profile);

            return;
        }

        /*
         * /tag <tag>
         */
        String value = args.get(0);

        if (value == null || value.isBlank()) {

            sendUsage(args);
            return;
        }

        value = value.trim();

        /*
         * /tag off
         *
         * Remove a tag escolhida.
         */
        if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("none")) {

            profile.setTag("");

            profileProvider.save(profile);

            args.sendMessage(ChatColor.GREEN + "Sua tag foi removida.");

            return;
        }

        /*
         * Procura o grupo correspondente
         * à tag informada.
         */
        Group group = findGroup(profile, value);

        if (group == null) {

            args.sendMessage(ChatColor.RED + "Você não possui essa tag.");

            return;
        }

        /*
         * Define a tag escolhida.
         *
         * IMPORTANTE:
         * o grupo não é alterado aqui.
         *
         * Estamos apenas escolhendo
         * qual tag será exibida.
         */
        profile.setTag(group.getAbbreviation());

        /*
         * Salva o perfil.
         */
        profileProvider.save(profile);

        /*
         * Confirma para o jogador.
         */
        args.sendMessage(ChatColor.GREEN + "Sua tag foi alterada para " + "§" + group.getColor() + group.getAbbreviation() + ChatColor.GREEN + ".");
    }

    /**
     * Autocomplete do /tag.
     */
    @Override
    public Completer getCompleter() {

        return args -> {

            /*
             * /tag <algo>
             */
            if (args.size() > 1) {
                return List.of();
            }

            String input;

            if (args.size() == 0) {
                input = "";
            } else {
                input = args.get(0);

                if (input == null) {
                    input = "";
                }
            }

            String search = input.toLowerCase(Locale.ROOT);

            /*
             * Neste ponto o Completer recebe
             * CommandArgs, não BukkitCommandArgs.
             *
             * Portanto não conseguimos pegar
             * diretamente o Player por aqui.
             *
             * O autocomplete apresenta as
             * possibilidades conhecidas.
             */
            List<String> tags = List.of("off", "ADM", "MOD++", "MOD", "TRIAL", "HLP", "STF", "DEV", "BLD", "INF", "PRO", "LEG+", "LEG", "EXP+", "EXP");

            return tags.stream().filter(tag -> tag.toLowerCase(Locale.ROOT).startsWith(search)).toList();
        };
    }

    /**
     * Procura o grupo correspondente
     * à tag informada.
     * <p>
     * Aceita:
     * <p>
     * ADM
     * ADMIN
     * LEG+
     * LEGENDPLUS
     * LEG
     * LEGEND
     * etc.
     */
    private Group findGroup(PlayerProfile profile, String value) {

        if (profile == null || value == null || value.isBlank()) {

            return null;
        }

        Account account = profile.getAccount();

        if (account == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        /*
         * Grupo permanente.
         */
        Group permanentGroup = account.getGroup();

        if (isMatchingGroup(permanentGroup, normalized)) {

            return permanentGroup;
        }

        /*
         * Grupos temporários.
         */
        for (Group group : account.getTemporaryGroups().keySet()) {

            if (group == null || group == Group.DEFAULT) {

                continue;
            }

            /*
             * Não permite grupo temporário
             * expirado.
             */
            if (!account.hasTemporaryGroup(group)) {
                continue;
            }

            if (isMatchingGroup(group, normalized)) {

                return group;
            }
        }

        return null;
    }

    /**
     * Verifica se o valor informado
     * corresponde ao grupo.
     */
    private boolean isMatchingGroup(Group group, String value) {

        if (group == null || group == Group.DEFAULT || value == null || value.isBlank()) {

            return false;
        }

        /*
         * Nome do enum.
         *
         * Ex:
         * LEGEND
         * LEGENDPLUS
         * MODPLUS
         */
        if (group.name().equalsIgnoreCase(value)) {

            return true;
        }

        /*
         * Abreviação.
         *
         * Ex:
         * LEG
         * LEG+
         * MOD++
         * ADM
         */
        if (group.getAbbreviation().equalsIgnoreCase(value)) {

            return true;
        }

        /*
         * Nome de exibição.
         *
         * Ex:
         * LEGEND
         * LEGEND+
         * MOD++
         */
        if (group.getDisplayName().equalsIgnoreCase(value)) {

            return true;
        }

        return false;
    }

    /**
     * Lista as tags disponíveis
     * para o jogador.
     */
    private void sendAvailableTags(BukkitCommandArgs args, PlayerProfile profile) {

        args.getSender().sendMessage("");

        args.getSender().sendMessage(ChatColor.AQUA + "§lLABOON" + ChatColor.GRAY + " » " + ChatColor.WHITE + "Suas tags disponíveis:");

        boolean hasTag = false;

        Account account = profile.getAccount();

        /*
         * Grupo permanente.
         */
        Group permanentGroup = account.getGroup();

        if (permanentGroup != null && permanentGroup != Group.DEFAULT) {

            sendTag(args, permanentGroup);

            hasTag = true;
        }

        /*
         * Grupos temporários.
         */
        for (Group group : account.getTemporaryGroups().keySet()) {

            if (group == null || group == Group.DEFAULT) {

                continue;
            }

            /*
             * Não mostra grupo temporário
             * expirado.
             */
            if (!account.hasTemporaryGroup(group)) {
                continue;
            }

            /*
             * Evita duplicar a tag.
             */
            if (group == permanentGroup) {
                continue;
            }

            sendTag(args, group);

            hasTag = true;
        }

        /*
         * Opção para remover a tag.
         */
        args.getSender().sendMessage(ChatColor.GRAY + "§8• " + ChatColor.WHITE + "off" + ChatColor.GRAY + " - remover tag");

        /*
         * Nenhuma tag disponível.
         */
        if (!hasTag) {

            args.getSender().sendMessage(ChatColor.GRAY + "Você não possui nenhuma tag.");
        }

        args.getSender().sendMessage("");
    }

    /**
     * Mostra uma tag na lista.
     */
    private void sendTag(BukkitCommandArgs args, Group group) {

        args.getSender().sendMessage(ChatColor.GRAY + "§8• " + "§" + group.getColor() + group.getAbbreviation() + ChatColor.GRAY + " - " + ChatColor.WHITE + group.getDisplayName());
    }

    /**
     * Mensagem de uso.
     */
    private void sendUsage(BukkitCommandArgs args) {

        args.sendMessage(ChatColor.YELLOW + "Uso: /tag <tag|off>");
    }
}