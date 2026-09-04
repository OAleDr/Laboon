package br.com.laboon.bukkit.display;

import br.com.laboon.bukkit.config.ServerConfig;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.profile.PlayerProfile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class DisplayManager {

    private static final String NETWORK_ADDRESS =
            "play.laboon.com.br";

    private final ProfileProvider profileProvider;
    private final ServerConfig serverConfig;
    private final Scoreboard scoreboard;

    public DisplayManager(
            ProfileProvider profileProvider,
            ServerConfig serverConfig
    ) {

        if (profileProvider == null) {
            throw new IllegalArgumentException(
                    "O ProfileProvider não pode ser nulo."
            );
        }

        if (serverConfig == null) {
            throw new IllegalArgumentException(
                    "O ServerConfig não pode ser nulo."
            );
        }

        if (Bukkit.getScoreboardManager() == null) {
            throw new IllegalStateException(
                    "O ScoreboardManager não está disponível."
            );
        }

        this.profileProvider = profileProvider;
        this.serverConfig = serverConfig;

        this.scoreboard =
                Bukkit.getScoreboardManager()
                        .getMainScoreboard();

        initializeTeams();
    }

    public void shutdown() {

        for (Team team : scoreboard.getTeams()) {

            if (!team.getName().startsWith("tab-")) {
                continue;
            }

            team.getEntries().forEach(team::removeEntry);

            team.unregister();
        }
    }

    public void update(Player player) {

        if (player == null) {
            return;
        }

        updateHeaderAndFooter(player);
        updateTabName(player);
        updateNameTag(player);
        updatePlayerTeam(player);
    }

    public void updateAll() {

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            update(player);
        }
    }

    public void removePlayer(Player player) {

        if (player == null) {
            return;
        }

        String entry =
                player.getName();

        for (Team team :
                scoreboard.getTeams()) {

            if (team.hasEntry(entry)) {
                team.removeEntry(entry);
            }
        }
    }

    private void initializeTeams() {

        for (Group group :
                Group.values()) {

            String teamName =
                    getTeamName(group);

            Team team =
                    scoreboard.getTeam(teamName);

            if (team == null) {

                team =
                        scoreboard.registerNewTeam(
                                teamName
                        );
            }

            team.setOption(
                    Team.Option.NAME_TAG_VISIBILITY,
                    Team.OptionStatus.ALWAYS
            );
        }
    }

    private void updateHeaderAndFooter(
            Player player
    ) {

        Component header =
                Component.text()
                        .append(
                                Component.newline()
                        )
                        .append(
                                Component.text(
                                        "LABOON",
                                        NamedTextColor.AQUA
                                )
                        )
                        .append(
                                Component.newline()
                        )
                        .append(
                                Component.text(
                                        "Uma nova experiência.",
                                        NamedTextColor.GRAY
                                )
                        )
                        .append(
                                Component.newline()
                        )
                        .build();

        Component footer =
                Component.text()
                        .append(
                                Component.newline()
                        )
                        .append(
                                Component.text(
                                        "Servidor: ",
                                        NamedTextColor.GRAY
                                )
                        )
                        .append(
                                Component.text(
                                        serverConfig.getServerName(),
                                        NamedTextColor.WHITE
                                )
                        )
                        .append(
                                Component.newline()
                        )
                        .append(
                                Component.text(
                                        "Jogadores: ",
                                        NamedTextColor.GRAY
                                )
                        )
                        .append(
                                Component.text(
                                        String.valueOf(
                                                player.getServer()
                                                        .getOnlinePlayers()
                                                        .size()
                                        ),
                                        NamedTextColor.WHITE
                                )
                        )
                        .append(
                                Component.newline()
                        )
                        .append(
                                Component.text(
                                        "Ping: ",
                                        NamedTextColor.GRAY
                                )
                        )
                        .append(
                                Component.text(
                                        String.valueOf(
                                                getPing(player)
                                        ),
                                        NamedTextColor.WHITE
                                )
                        )
                        .append(
                                Component.text(
                                        "ms",
                                        NamedTextColor.GRAY
                                )
                        )
                        .append(
                                Component.newline()
                        )
                        .append(
                                Component.newline()
                        )
                        .append(
                                Component.text(
                                        NETWORK_ADDRESS,
                                        NamedTextColor.AQUA
                                )
                        )
                        .build();

        player.sendPlayerListHeaderAndFooter(
                header,
                footer
        );
    }

    private void updateTabName(
            Player player
    ) {

        player.playerListName(
                getDisplayName(player)
        );
    }

    private void updateNameTag(
            Player player
    ) {

        Team team =
                getPlayerTeam(player);

        if (team == null) {
            return;
        }

        /*
         * O prefixo usa a TAG escolhida pelo jogador.
         */
        team.prefix(
                getTag(player)
        );
    }

    private void updatePlayerTeam(
            Player player
    ) {

        Team targetTeam =
                getPlayerTeam(player);

        if (targetTeam == null) {
            return;
        }

        String entry =
                player.getName();

        /*
         * Remove o jogador de qualquer Team anterior.
         */
        for (Team team :
                scoreboard.getTeams()) {

            if (team.equals(targetTeam)) {
                continue;
            }

            if (team.hasEntry(entry)) {
                team.removeEntry(entry);
            }
        }

        /*
         * Adiciona ao Team correspondente
         * ao grupo efetivo.
         */
        if (!targetTeam.hasEntry(entry)) {

            targetTeam.addEntry(entry);
        }
    }

    private Team getPlayerTeam(
            Player player
    ) {

        PlayerProfile profile =
                profileProvider.getProfile(
                        player
                );

        Group group =
                profile == null
                        ? Group.DEFAULT
                        : profile.getGroup();

        return scoreboard.getTeam(
                getTeamName(group)
        );
    }

    private Component getDisplayName(Player player) {

        PlayerProfile profile =
                profileProvider.getProfile(player);

        if (profile == null) {

            return Component.text(
                    player.getName(),
                    NamedTextColor.WHITE
            );
        }

        String tag =
                profile.getTag();

        /*
         * Sem tag selecionada.
         *
         * /tag off
         */
        if (tag == null || tag.isBlank()) {

            return Component.text(
                    player.getName(),
                    NamedTextColor.WHITE
            );
        }

        Group tagGroup =
                findGroupByTag(tag);

        TextColor tagColor =
                TextColor.color(
                        getMinecraftColor(
                                tagGroup != null
                                        ? tagGroup.getColor()
                                        : profile.getGroup().getColor()
                        )
                );

        return Component.textOfChildren(
                Component.text(
                        tag,
                        tagColor
                ),
                Component.space(),
                Component.text(
                        player.getName(),
                        NamedTextColor.WHITE
                )
        );
    }

    private Component getTag(Player player) {

        PlayerProfile profile =
                profileProvider.getProfile(player);

        if (profile == null) {
            return Component.empty();
        }

        String tag =
                profile.getTag();

        /*
         * /tag off
         *
         * Não mostra prefixo na NameTag.
         */
        if (tag == null || tag.isBlank()) {

            return Component.empty();
        }

        Group tagGroup =
                findGroupByTag(tag);

        char color =
                tagGroup != null
                        ? tagGroup.getColor()
                        : profile.getGroup().getColor();

        TextColor tagColor =
                TextColor.color(
                        getMinecraftColor(color)
                );

        return Component.text(
                tag + " ",
                tagColor
        );
    }

    private Group findGroupByTag(
            String tag
    ) {

        if (tag == null
                || tag.isBlank()) {

            return null;
        }

        for (Group group :
                Group.values()) {

            if (group == Group.DEFAULT) {
                continue;
            }

            if (group.getAbbreviation()
                    .equalsIgnoreCase(
                            tag.trim()
                    )) {

                return group;
            }
        }

        return null;
    }

    public String getTabTeam(
            Player player
    ) {

        PlayerProfile profile =
                profileProvider.getProfile(
                        player
                );

        if (profile == null) {

            return getTeamName(
                    Group.DEFAULT
            );
        }

        return getTeamName(
                profile.getGroup()
        );
    }

    private String getTeamName(
            Group group
    ) {

        int order =
                100 - group.getPower();

        return String.format(
                "tab-%03d-%s",
                order,
                group.name().toLowerCase()
        );
    }

    private int getPing(
            Player player
    ) {

        return player.getPing();
    }

    private int getMinecraftColor(
            char color
    ) {

        return switch (
                Character.toLowerCase(color)
                ) {

            case '0' -> 0x000000;
            case '1' -> 0x0000AA;
            case '2' -> 0x00AA00;
            case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000;
            case '5' -> 0xAA00AA;
            case '6' -> 0xFFAA00;
            case '7' -> 0xAAAAAA;
            case '8' -> 0x555555;
            case '9' -> 0x5555FF;
            case 'a' -> 0x55FF55;
            case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555;
            case 'd' -> 0xFF55FF;
            case 'e' -> 0xFFFF55;
            case 'f' -> 0xFFFFFF;

            default ->
                    0xAAAAAA;
        };
    }
}