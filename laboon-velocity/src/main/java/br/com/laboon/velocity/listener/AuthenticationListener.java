package br.com.laboon.velocity.listener;

import br.com.laboon.velocity.auth.MojangProfileService;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;

import java.util.UUID;

public final class AuthenticationListener {

    private final MojangProfileService mojangProfileService;

    public AuthenticationListener(MojangProfileService mojangProfileService) {
        this.mojangProfileService = mojangProfileService;
    }

    @Subscribe
    public EventTask onPreLogin(PreLoginEvent event) {

        return EventTask.async(() -> {

            String username = event.getUsername();

            try {

                /*
                 * Consulta o nickname na Mojang.
                 *
                 * UUID != null
                 *     -> jogador ORIGINAL
                 *
                 * UUID == null
                 *     -> jogador LABOON
                 */
                UUID uuid = mojangProfileService.findUuid(username);

                if (uuid != null) {

                    /*
                     * Perfil encontrado na Mojang.
                     *
                     * O Velocity deverá autenticar
                     * a conexão normalmente contra a Mojang.
                     */
                    event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());

                    System.out.println("[Laboon] " + username + " -> ORIGINAL" + " | UUID=" + uuid);

                } else {

                    /*
                     * Perfil não encontrado.
                     *
                     * A conexão será tratada como
                     * jogador offline/cracked.
                     */
                    event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());

                    System.out.println("[Laboon] " + username + " -> LABOON");
                }

            } catch (Exception exception) {

                /*
                 * Falha na consulta da Mojang NÃO deve
                 * transformar o jogador em cracked.
                 */
                exception.printStackTrace();

                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(net.kyori.adventure.text.Component.text("Não foi possível verificar sua conta. " + "Tente novamente em alguns instantes.")));
            }
        });
    }
}