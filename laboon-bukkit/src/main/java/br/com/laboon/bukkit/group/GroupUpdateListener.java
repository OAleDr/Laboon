package br.com.laboon.bukkit.group;

import br.com.laboon.bukkit.display.DisplayManager;
import br.com.laboon.bukkit.profile.ProfileProvider;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.account.group.GroupUpdatePublisher;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.profile.PlayerProfile;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GroupUpdateListener {

    private final Plugin plugin;
    private final ProfileProvider profileProvider;
    private final DisplayManager displayManager;
    private final MessageBus messageBus;

    public GroupUpdateListener(Plugin plugin, ProfileProvider profileProvider, DisplayManager displayManager, MessageBus messageBus) {

        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo.");
        }

        if (profileProvider == null) {
            throw new IllegalArgumentException("ProfileProvider não pode ser nulo.");
        }

        if (displayManager == null) {
            throw new IllegalArgumentException("DisplayManager não pode ser nulo.");
        }

        if (messageBus == null) {
            throw new IllegalArgumentException("MessageBus não pode ser nulo.");
        }

        this.plugin = plugin;

        this.profileProvider = profileProvider;

        this.displayManager = displayManager;

        this.messageBus = messageBus;
    }

    public void register() {

        messageBus.subscribe(GroupUpdatePublisher.CHANNEL, this::handle);
    }

    private void handle(String channel, String message) {

        if (!GroupUpdatePublisher.CHANNEL.equals(channel)) {

            return;
        }

        GroupUpdateData data;

        try {

            data = deserialize(message);

        } catch (Exception exception) {

            plugin.getLogger().warning("Não foi possível processar atualização de grupo: " + exception.getMessage());

            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> apply(data));
    }

    private void apply(GroupUpdateData data) {

        Player player = Bukkit.getPlayer(data.uniqueId());

        /*
         * Jogador não está neste servidor.
         */
        if (player == null) {
            return;
        }

        PlayerProfile profile = profileProvider.getProfile(player);

        if (profile == null) {

            plugin.getLogger().warning("Perfil não encontrado para " + player.getName());

            return;
        }

        /*
         * =====================================================
         * GRUPO PERMANENTE
         * =====================================================
         */
        profile.setGroup(data.permanentGroup());

        /*
         * =====================================================
         * TAG
         * =====================================================
         *
         * Agora sincronizamos também a tag.
         */
        profile.setTag(data.tag());

        /*
         * =====================================================
         * GRUPOS TEMPORÁRIOS
         * =====================================================
         */

        for (Group group : profile.getAccount().getTemporaryGroups().keySet().toArray(Group[]::new)) {

            profile.getAccount().removeTemporaryGroup(group);
        }

        for (Map.Entry<Group, Instant> entry : data.temporaryGroups().entrySet()) {

            profile.getAccount().setTemporaryGroup(entry.getKey(), entry.getValue());
        }

        /*
         * Salva o estado atualizado.
         */
        profileProvider.save(profile);

        /*
         * =====================================================
         * ATUALIZAÇÃO VISUAL IMEDIATA
         * =====================================================
         *
         * - TAB
         * - NameTag
         * - Team
         */
        displayManager.update(player);

        /*
         * Informa o jogador.
         */
        player.sendMessage("§bLABOON §8» §fSeu grupo foi atualizado para §e" + formatGroup(profile.getGroup()) + "§f.");
    }

    private GroupUpdateData deserialize(String message) {

        if (message == null || message.isBlank()) {

            throw new IllegalArgumentException("Mensagem vazia.");
        }

        String[] parts = message.split("\\|", -1);

        if (parts.length < 3) {

            throw new IllegalArgumentException("Formato de mensagem inválido.");
        }

        /*
         * UUID
         */
        UUID uniqueId;

        try {

            uniqueId = UUID.fromString(parts[0]);

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException("UUID inválido.");
        }

        /*
         * Grupo permanente
         */
        Group permanentGroup;

        try {

            permanentGroup = Group.valueOf(parts[1].trim().toUpperCase());

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException("Grupo permanente inválido.");
        }

        /*
         * Tag
         */
        String tag = parts[2] == null ? "" : parts[2].trim();

        /*
         * Grupos temporários
         */
        Map<Group, Instant> temporaryGroups = new HashMap<>();

        for (int index = 3; index < parts.length; index++) {

            String value = parts[index];

            int separator = value.indexOf(':');

            if (separator <= 0 || separator >= value.length() - 1) {

                continue;
            }

            String groupName = value.substring(0, separator);

            String expirationValue = value.substring(separator + 1);

            Group group;

            try {

                group = Group.valueOf(groupName.trim().toUpperCase());

            } catch (IllegalArgumentException exception) {

                continue;
            }

            Instant expiresAt;

            try {

                expiresAt = Instant.parse(expirationValue);

            } catch (Exception exception) {

                continue;
            }

            /*
             * Ignora grupos expirados.
             */
            if (!expiresAt.isAfter(Instant.now())) {

                continue;
            }

            temporaryGroups.put(group, expiresAt);
        }

        return new GroupUpdateData(uniqueId, permanentGroup, tag, temporaryGroups);
    }

    private String formatGroup(Group group) {

        if (group == null || group == Group.DEFAULT) {

            return "DEFAULT";
        }

        return group.getDisplayName();
    }

    private record GroupUpdateData(UUID uniqueId, Group permanentGroup, String tag,
                                   Map<Group, Instant> temporaryGroups) {
    }
}