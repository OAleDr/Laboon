package br.com.laboon.bukkit.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TitleAPI implements Listener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Map<UUID, TitleState> STATES = new ConcurrentHashMap<>();

    /**
     * Define título, subtítulo e duração.
     *
     * @param ticks se true, os tempos são interpretados como ticks.
     *              se false, os tempos são interpretados como segundos.
     */
    public static void setTitle(Player player, String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime, boolean ticks) {
        if (player == null) {
            return;
        }

        Component titleComponent = title == null || title.isEmpty() ? Component.empty() : parse(title);

        Component subtitleComponent = subtitle == null || subtitle.isEmpty() ? Component.empty() : parse(subtitle);

        Title.Times times = createTimes(fadeInTime, stayTime, fadeOutTime, ticks);

        Title adventureTitle = Title.title(titleComponent, subtitleComponent, times);

        STATES.put(player.getUniqueId(), new TitleState(titleComponent, subtitleComponent, times));

        player.showTitle(adventureTitle);
    }

    /**
     * Define título, subtítulo e duração em segundos.
     */
    public static void setTitle(Player player, String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
        setTitle(player, title, subtitle, fadeInTime, stayTime, fadeOutTime, false);
    }

    /**
     * Define título e subtítulo mantendo o tempo padrão do cliente.
     */
    public static void setTitle(Player player, String title, String subtitle) {
        if (player == null) {
            return;
        }

        Component titleComponent = title == null || title.isEmpty() ? Component.empty() : parse(title);

        Component subtitleComponent = subtitle == null || subtitle.isEmpty() ? Component.empty() : parse(subtitle);

        Title adventureTitle = Title.title(titleComponent, subtitleComponent);

        STATES.put(player.getUniqueId(), new TitleState(titleComponent, subtitleComponent, null));

        player.showTitle(adventureTitle);
    }

    /**
     * Define somente o título.
     */
    public static void setTitle(Player player, String title) {
        if (player == null || title == null) {
            return;
        }

        Component titleComponent = parse(title);

        TitleState current = STATES.get(player.getUniqueId());

        Component subtitle = current == null ? Component.empty() : current.subtitle();

        Title.Times times = current == null ? null : current.times();

        Title adventureTitle;

        if (times != null) {
            adventureTitle = Title.title(titleComponent, subtitle, times);
        } else {
            adventureTitle = Title.title(titleComponent, subtitle);
        }

        STATES.put(player.getUniqueId(), new TitleState(titleComponent, subtitle, times));

        player.showTitle(adventureTitle);
    }

    /**
     * Define somente o título com duração.
     */
    public static void setTitle(Player player, String title, int fadeInTime, int stayTime, int fadeOutTime) {
        if (player == null || title == null) {
            return;
        }

        Component titleComponent = parse(title);

        TitleState current = STATES.get(player.getUniqueId());

        Component subtitle = current == null ? Component.empty() : current.subtitle();

        Title.Times times = createTimes(fadeInTime, stayTime, fadeOutTime, false);

        Title adventureTitle = Title.title(titleComponent, subtitle, times);

        STATES.put(player.getUniqueId(), new TitleState(titleComponent, subtitle, times));

        player.showTitle(adventureTitle);
    }

    /**
     * Define somente o subtítulo.
     */
    public static void setSubtitle(Player player, String subtitle) {
        if (player == null || subtitle == null) {
            return;
        }

        Component subtitleComponent = parse(subtitle);

        TitleState current = STATES.get(player.getUniqueId());

        Component title = current == null ? Component.empty() : current.title();

        Title.Times times = current == null ? null : current.times();

        Title adventureTitle;

        if (times != null) {
            adventureTitle = Title.title(title, subtitleComponent, times);
        } else {
            adventureTitle = Title.title(title, subtitleComponent);
        }

        STATES.put(player.getUniqueId(), new TitleState(title, subtitleComponent, times));

        player.showTitle(adventureTitle);
    }

    /**
     * Define somente os tempos do título atual.
     * <p>
     * Se nenhum título tiver sido registrado pela API,
     * o método não faz nada.
     */
    public static void setTimes(Player player, int fadeInTime, int stayTime, int fadeOutTime, boolean ticks) {
        if (player == null) {
            return;
        }

        TitleState current = STATES.get(player.getUniqueId());

        if (current == null) {
            return;
        }

        Title.Times times = createTimes(fadeInTime, stayTime, fadeOutTime, ticks);

        Title adventureTitle = Title.title(current.title(), current.subtitle(), times);

        STATES.put(player.getUniqueId(), new TitleState(current.title(), current.subtitle(), times));

        player.showTitle(adventureTitle);
    }

    /**
     * Remove o título atual e limpa o estado armazenado.
     */
    public static void resetTitle(Player player) {
        if (player == null) {
            return;
        }

        player.resetTitle();

        STATES.remove(player.getUniqueId());
    }

    /**
     * Limpa o título atual.
     */
    public static void clearTitle(Player player) {
        if (player == null) {
            return;
        }

        player.clearTitle();

        STATES.remove(player.getUniqueId());
    }

    private static Component parse(String text) {
        return MINI_MESSAGE.deserialize(text);
    }

    private static Title.Times createTimes(int fadeInTime, int stayTime, int fadeOutTime, boolean ticks) {
        return Title.Times.times(toDuration(fadeInTime, ticks), toDuration(stayTime, ticks), toDuration(fadeOutTime, ticks));
    }

    private static Duration toDuration(int value, boolean ticks) {
        if (value < 0) {
            value = 0;
        }

        if (ticks) {
            return Duration.ofMillis(value * 50L);
        }

        return Duration.ofSeconds(value);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        STATES.remove(event.getPlayer().getUniqueId());
    }

    private record TitleState(Component title, Component subtitle, Title.Times times) {
    }
}