package br.com.laboon.bukkit.chat;

import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.profile.PlayerProfile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.entity.Player;

public final class ChatFormatter {

    private final ProfileProvider profileProvider;

    public ChatFormatter(ProfileProvider profileProvider) {

        if (profileProvider == null) {
            throw new IllegalArgumentException(
                    "O ProfileProvider não pode ser nulo."
            );
        }

        this.profileProvider = profileProvider;
    }

    public Component format(
            Player player,
            String message
    ) {

        if (player == null) {
            throw new IllegalArgumentException(
                    "O jogador não pode ser nulo."
            );
        }

        if (message == null) {
            throw new IllegalArgumentException(
                    "A mensagem não pode ser nula."
            );
        }

        PlayerProfile profile =
                profileProvider.getProfile(player);

        if (profile == null) {

            return Component.textOfChildren(
                    Component.text(
                            player.getName(),
                            NamedTextColor.WHITE
                    ),
                    Component.text(
                            ": ",
                            NamedTextColor.GRAY
                    ),
                    Component.text(
                            message,
                            NamedTextColor.WHITE
                    )
            );
        }

        Group group = profile.getGroup();

        String tag = profile.getTag();

        TextColor tagColor = TextColor.color(
                getMinecraftColor(
                        group.getColor()
                )
        );

        Component tagComponent =
                Component.empty();

        if (tag != null && !tag.isBlank()) {

            tagComponent = Component.text(
                    tag,
                    tagColor
            ).append(
                    Component.space()
            );
        }

        return Component.textOfChildren(
                tagComponent,
                Component.text(
                        player.getName(),
                        NamedTextColor.WHITE
                ),
                Component.text(
                        ": ",
                        NamedTextColor.GRAY
                ),
                Component.text(
                        message,
                        NamedTextColor.WHITE
                )
        );
    }

    private int getMinecraftColor(char color) {

        return switch (Character.toLowerCase(color)) {

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

            default -> 0xAAAAAA;
        };
    }
}