package br.com.laboon.bukkit.command.commands;

import br.com.laboon.bukkit.command.BukkitCommandArgs;
import br.com.laboon.bukkit.vanish.VanishService;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ModeratorCommand implements CommandClass {

    private final VanishService vanishService;

    public ModeratorCommand(
            VanishService vanishService
    ) {
        this.vanishService = vanishService;
    }

    /*
     * ============================================================
     * /MODERATOR
     * ============================================================
     */

    @Command(
            name = "moderator",
            aliases = {"mod", "moderador"},
            description = "Comandos de moderação.",
            usage = "/moderator <subcomando>",
            group = Group.MOD,
            subcommands = {
                    "vanish",
                    "gamemode",
                    "gm",
                    "teleport",
                    "tp",
                    "teleportall",
                    "tpall",
                    "tphere",
                    "s"
            }
    )
    public void execute(BukkitCommandArgs args) {

        if (!args.isPlayer()) {
            args.sendMessage("§cEste comando só pode ser usado por jogadores.");
            return;
        }

        String subcommand = args.getSubcommand();

        if (subcommand == null) {
            sendHelp(args);
            return;
        }

        switch (subcommand.toLowerCase()) {

            case "vanish" -> handleVanish(args);

            case "gamemode", "gm" -> handleGamemode(args);

            case "teleport", "tp" -> handleTeleport(args);

            case "teleportall", "tpall" -> handleTeleportAll(args);

            case "tphere", "s" -> handleTeleportHere(args);

            default -> sendHelp(args);
        }
    }

    /*
     * ============================================================
     * VANISH
     * ============================================================
     */

    private void handleVanish(BukkitCommandArgs args) {

        Player player = args.getSender().getPlayer();

        if (!hasPermission(player, Group.MOD)) {
            args.sendMessage("§cVocê não possui permissão para utilizar este comando.");
            return;
        }

        vanishService.toggle(player);
    }

    /*
     * ============================================================
     * GAMEMODE
     *
     * /gm 0
     * /gm 1
     * /gm 2
     * /gm 3
     *
     * /gm creative
     * /gm survival
     *
     * /gm 1 Player
     * ============================================================
     */

    private void handleGamemode(BukkitCommandArgs args) {

        Player sender = args.getSender().getPlayer();

        if (!hasPermission(sender, Group.MOD)) {
            args.sendMessage("§cVocê não possui permissão para utilizar este comando.");
            return;
        }

        String[] arguments = args.getArguments().toArray(new String[0]);

        if (arguments.length == 0) {
            sender.sendMessage(
                    "§eUso: /gm <0|1|2|3|modo> [jogador]"
            );
            return;
        }

        GameMode gameMode = parseGameMode(arguments[0]);

        if (gameMode == null) {
            sender.sendMessage("§cModo de jogo inválido.");
            sender.sendMessage(
                    "§7Modos: §fsurvival, creative, adventure, spectator"
            );
            return;
        }

        /*
         * /gm 1
         */
        if (arguments.length == 1) {

            if (sender.getGameMode() == gameMode) {
                sender.sendMessage(
                        "§eVocê já está no modo §f"
                                + gameMode.name().toLowerCase()
                                + "§e."
                );
                return;
            }

            sender.setGameMode(gameMode);

            sender.sendMessage(
                    "§aSeu modo de jogo foi alterado para §f"
                            + gameMode.name().toLowerCase()
                            + "§a."
            );

            return;
        }

        /*
         * /gm 1 Player
         */
        Player target = Bukkit.getPlayerExact(arguments[1]);

        if (target == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        if (target.getGameMode() == gameMode) {
            sender.sendMessage(
                    "§eO jogador §f"
                            + target.getName()
                            + " §ejá está neste modo de jogo."
            );
            return;
        }

        target.setGameMode(gameMode);

        sender.sendMessage(
                "§aO modo de jogo de §f"
                        + target.getName()
                        + " §afoi alterado para §f"
                        + gameMode.name().toLowerCase()
                        + "§a."
        );

        target.sendMessage(
                "§eSeu modo de jogo foi alterado para §f"
                        + gameMode.name().toLowerCase()
                        + "§e por §f"
                        + sender.getName()
                        + "§e."
        );
    }

    private GameMode parseGameMode(String value) {

        /*
         * Nome
         */
        try {
            return GameMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        /*
         * Número
         */
        try {

            int number = Integer.parseInt(value);

            return switch (number) {
                case 0 -> GameMode.SURVIVAL;
                case 1 -> GameMode.CREATIVE;
                case 2 -> GameMode.ADVENTURE;
                case 3 -> GameMode.SPECTATOR;
                default -> null;
            };

        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /*
     * ============================================================
     * TELEPORT
     *
     * /tp Player
     * /tp Player1 Player2
     * /tp x y z
     * /tp Player x y z
     * ============================================================
     */

    private void handleTeleport(BukkitCommandArgs args) {

        Player sender = args.getSender().getPlayer();

        if (!hasPermission(sender, Group.MOD)) {
            args.sendMessage("§cVocê não possui permissão para utilizar este comando.");
            return;
        }

        String[] arguments = args.getArguments().toArray(new String[0]);

        if (arguments.length == 0) {
            sender.sendMessage(
                    "§eUso:"
                            + "\n§7/tp <jogador>"
                            + "\n§7/tp <jogador> <jogador>"
                            + "\n§7/tp <x> <y> <z>"
                            + "\n§7/tp <jogador> <x> <y> <z>"
            );
            return;
        }

        /*
         * /tp Player
         */
        if (arguments.length == 1) {

            Player target = Bukkit.getPlayerExact(arguments[0]);

            if (target == null) {
                sender.sendMessage("§cJogador não encontrado.");
                return;
            }

            sender.teleport(target.getLocation());

            sender.sendMessage(
                    "§aVocê foi teleportado até §f"
                            + target.getName()
                            + "§a."
            );

            return;
        }

        /*
         * /tp Player1 Player2
         */
        if (arguments.length == 2) {

            Player player = Bukkit.getPlayerExact(arguments[0]);

            if (player == null) {
                sender.sendMessage("§cJogador não encontrado: §f" + arguments[0]);
                return;
            }

            Player target = Bukkit.getPlayerExact(arguments[1]);

            if (target == null) {
                sender.sendMessage("§cJogador não encontrado: §f" + arguments[1]);
                return;
            }

            player.teleport(target.getLocation());

            sender.sendMessage(
                    "§aVocê teleportou §f"
                            + player.getName()
                            + " §aaté §f"
                            + target.getName()
                            + "§a."
            );

            return;
        }

        /*
         * /tp x y z
         */
        if (arguments.length == 3) {

            Location location = getLocationBased(
                    sender.getLocation(),
                    arguments[0],
                    arguments[1],
                    arguments[2]
            );

            if (location == null) {
                sender.sendMessage("§cCoordenadas inválidas.");
                return;
            }

            sender.teleport(location);

            sender.sendMessage(
                    "§aTeleportado para §f"
                            + format(location.getX())
                            + " "
                            + format(location.getY())
                            + " "
                            + format(location.getZ())
                            + "§a."
            );

            return;
        }

        /*
         * /tp Player x y z
         */
        if (arguments.length == 4) {

            Player target = Bukkit.getPlayerExact(arguments[0]);

            if (target == null) {
                sender.sendMessage(
                        "§cJogador não encontrado: §f"
                                + arguments[0]
                );
                return;
            }

            Location location = getLocationBased(
                    target.getLocation(),
                    arguments[1],
                    arguments[2],
                    arguments[3]
            );

            if (location == null) {
                sender.sendMessage("§cCoordenadas inválidas.");
                return;
            }

            target.teleport(location);

            sender.sendMessage(
                    "§aVocê teleportou §f"
                            + target.getName()
                            + " §apara §f"
                            + format(location.getX())
                            + " "
                            + format(location.getY())
                            + " "
                            + format(location.getZ())
                            + "§a."
            );

            return;
        }

        sender.sendMessage("§cQuantidade de argumentos inválida.");
    }

    /*
     * ============================================================
     * TELEPORT ALL
     *
     * /tpall
     * /tpall Player
     * ============================================================
     */

    private void handleTeleportAll(BukkitCommandArgs args) {

        Player sender = args.getSender().getPlayer();

        if (!hasPermission(sender, Group.MOD)) {
            args.sendMessage("§cVocê não possui permissão para utilizar este comando.");
            return;
        }

        String[] arguments = args.getArguments().toArray(new String[0]);

        Player destination = sender;

        /*
         * /tpall Player
         */
        if (arguments.length >= 1) {

            destination = Bukkit.getPlayerExact(arguments[0]);

            if (destination == null) {
                sender.sendMessage(
                        "§cJogador não encontrado: §f"
                                + arguments[0]
                );
                return;
            }
        }

        int teleported = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.getUniqueId().equals(sender.getUniqueId())) {
                continue;
            }

            if (player.getUniqueId().equals(destination.getUniqueId())) {
                continue;
            }

            player.teleport(destination.getLocation());
            player.setFallDistance(0.0F);

            player.sendMessage(
                    "§eVocê foi teleportado até §f"
                            + destination.getName()
                            + "§e."
            );

            teleported++;
        }

        sender.sendMessage(
                "§a"
                        + teleported
                        + " jogador(es) foram teleportados até §f"
                        + destination.getName()
                        + "§a."
        );
    }

    /*
     * ============================================================
     * TPHERE
     *
     * /tphere Player
     * ============================================================
     */

    private void handleTeleportHere(BukkitCommandArgs args) {

        Player sender = args.getSender().getPlayer();

        if (!hasPermission(sender, Group.MOD)) {
            args.sendMessage("§cVocê não possui permissão para utilizar este comando.");
            return;
        }

        String[] arguments = args.getArguments().toArray(new String[0]);

        if (arguments.length != 1) {
            sender.sendMessage("§eUso: /tphere <jogador>");
            return;
        }

        Player target = Bukkit.getPlayerExact(arguments[0]);

        if (target == null) {
            sender.sendMessage(
                    "§cJogador não encontrado: §f"
                            + arguments[0]
            );
            return;
        }

        target.teleport(sender.getLocation());
        target.setFallDistance(0.0F);

        sender.sendMessage(
                "§aVocê teleportou §f"
                        + target.getName()
                        + " §aaté você."
        );

        target.sendMessage(
                "§eVocê foi teleportado até §f"
                        + sender.getName()
                        + "§e."
        );
    }

    /*
     * ============================================================
     * COORDENADAS
     *
     * 100 70 100
     * ~ ~ ~
     * ~10 ~-5 ~
     * ============================================================
     */

    private Location getLocationBased(
            Location base,
            String argX,
            String argY,
            String argZ
    ) {

        Double x = parseCoordinate(base.getX(), argX);
        Double y = parseCoordinate(base.getY(), argY);
        Double z = parseCoordinate(base.getZ(), argZ);

        if (x == null || y == null || z == null) {
            return null;
        }

        Location location = base.clone();

        location.setX(x);
        location.setY(y);
        location.setZ(z);

        return location;
    }

    private Double parseCoordinate(
            double base,
            String value
    ) {

        if (value == null || value.isEmpty()) {
            return null;
        }

        /*
         * Coordenada absoluta
         *
         * Ex:
         * 100
         * 64.5
         */
        if (!value.startsWith("~")) {

            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        /*
         * ~
         */
        if (value.equals("~")) {
            return base;
        }

        /*
         * ~10
         * ~-10
         * ~0.5
         */
        String offset = value.substring(1);

        try {
            return base + Double.parseDouble(offset);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /*
     * ============================================================
     * PERMISSION
     * ============================================================
     */

    private boolean hasPermission(
            Player player,
            Group group
    ) {

        /*
         * Adapte este método caso seu sistema de Group utilize
         * outra API.
         */
        return player.hasPermission(
                "laboon.group." + group.name().toLowerCase()
        );
    }

    /*
     * ============================================================
     * HELP
     * ============================================================
     */

    private void sendHelp(BukkitCommandArgs args) {

        Player player = args.getSender().getPlayer();

        player.sendMessage("");
        player.sendMessage("§8§m--------------------------------");
        player.sendMessage("§b§lLABOON §7» §fModeração");
        player.sendMessage("");
        player.sendMessage("§f/mod vanish §7- Alternar invisibilidade");
        player.sendMessage("§f/mod gm <modo> [jogador]");
        player.sendMessage("§f/mod tp <jogador>");
        player.sendMessage("§f/mod tp <jogador> <jogador>");
        player.sendMessage("§f/mod tp <x> <y> <z>");
        player.sendMessage("§f/mod tpall [jogador]");
        player.sendMessage("§f/mod tphere <jogador>");
        player.sendMessage("");
        player.sendMessage("§8§m--------------------------------");
        player.sendMessage("");
    }

    /*
     * ============================================================
     * FORMAT
     * ============================================================
     */

    private String format(double value) {

        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }

        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }
}