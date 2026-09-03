package br.com.laboon.bukkit.profile;

import br.com.laboon.core.profile.PlayerProfile;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ProfileListener implements Listener {

    private final ProfileProvider profileProvider;

    public ProfileListener(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        profileProvider.getProfile(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        PlayerProfile profile = profileProvider.getProfile(event.getPlayer());

        if (profile == null) {
            return;
        }

        profileProvider.save(profile);

        profileProvider.unload(event.getPlayer().getUniqueId());
    }
}