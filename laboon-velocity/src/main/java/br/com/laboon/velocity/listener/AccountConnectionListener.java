package br.com.laboon.velocity.listener;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.profile.PlayerProfile;
import br.com.laboon.core.profile.ProfileManager;
import br.com.laboon.velocity.account.VelocityAccountService;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;

public final class AccountConnectionListener {

    private final VelocityAccountService accountService;

    private final ProfileManager profileManager;

    public AccountConnectionListener(VelocityAccountService accountService, ProfileManager profileManager) {

        this.accountService = accountService;

        this.profileManager = profileManager;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {

        Player player = event.getPlayer();

        Account account = accountService.loadOrCreate(player);

        if (account == null) {

            throw new IllegalStateException("Não foi possível carregar a conta de " + player.getUsername());
        }

        PlayerProfile profile = profileManager.load(player.getUniqueId());

        if (profile == null) {

            throw new IllegalStateException("Não foi possível carregar o perfil de " + player.getUsername());
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {

        Player player = event.getPlayer();

        profileManager.unload(player.getUniqueId());

        accountService.logout(player);
    }
}