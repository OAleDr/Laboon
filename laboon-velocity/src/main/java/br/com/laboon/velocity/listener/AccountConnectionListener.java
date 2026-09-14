package br.com.laboon.velocity.listener;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.profile.PlayerProfile;
import br.com.laboon.core.profile.ProfileManager;
import br.com.laboon.velocity.account.VelocityAccountService;
import br.com.laboon.velocity.auth.AuthenticationResult;
import br.com.laboon.velocity.auth.AuthenticationService;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;

public final class AccountConnectionListener {

    private final VelocityAccountService accountService;
    private final ProfileManager profileManager;
    private final AuthenticationService authenticationService;

    public AccountConnectionListener(VelocityAccountService accountService, ProfileManager profileManager, AuthenticationService authenticationService) {

        this.accountService = accountService;
        this.profileManager = profileManager;
        this.authenticationService = authenticationService;
    }

    @Subscribe
    public EventTask onLogin(LoginEvent event) {

        Player player = event.getPlayer();

        return EventTask.async(() -> authenticatePlayer(event, player));
    }

    private void authenticatePlayer(LoginEvent event, Player player) {

        try {

            /*
             * =========================
             * AUTENTICAÇÃO
             * =========================
             */

            AuthenticationResult authentication = authenticationService.authenticate(player.getUsername());

            /*
             * =========================
             * LOG
             * =========================
             */

            if (authentication.isOriginal()) {

                System.out.println("[Laboon] " + player.getUsername() + " autenticado como ORIGINAL. UUID=" + authentication.getUuid());

            } else {

                System.out.println("[Laboon] " + player.getUsername() + " autenticado como LABOON. UUID=" + authentication.getUuid());
            }

            /*
             * =========================
             * CONTA
             * =========================
             */

            Account account = accountService.loadOrCreate(player, authentication);

            if (account == null) {

                event.setResult(LoginEvent.ComponentResult.denied(net.kyori.adventure.text.Component.text("Não foi possível carregar sua conta.")));

                return;
            }

            /*
             * =========================
             * PERFIL
             * =========================
             */

            PlayerProfile profile = profileManager.load(account.getUniqueId());

            if (profile == null) {

                event.setResult(LoginEvent.ComponentResult.denied(net.kyori.adventure.text.Component.text("Não foi possível carregar seu perfil.")));

                return;
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            event.setResult(LoginEvent.ComponentResult.denied(net.kyori.adventure.text.Component.text("Não foi possível autenticar sua conta. Tente novamente.")));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {

        Player player = event.getPlayer();

        profileManager.unload(player.getUniqueId());

        accountService.logout(player);
    }
}