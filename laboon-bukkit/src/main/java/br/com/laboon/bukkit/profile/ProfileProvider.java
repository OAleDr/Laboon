package br.com.laboon.bukkit.profile;

import br.com.laboon.core.profile.PlayerProfile;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface ProfileProvider {

    PlayerProfile getProfile(Player player);

    void save(PlayerProfile profile);

    void unload(UUID uniqueId);

    void saveAll();
}