package br.com.laboon.core.vanish;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class VanishPlayerFilter<T> {

    private final Function<T, UUID> uuidResolver;
    private final GlobalVanishRepository repository;

    public VanishPlayerFilter(
            Function<T, UUID> uuidResolver,
            GlobalVanishRepository repository
    ) {
        this.uuidResolver = uuidResolver;
        this.repository = repository;
    }

    public List<T> filter(Collection<T> players) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }

        return players.stream()
                .filter(player -> player != null)
                .filter(player -> {
                    UUID uuid = uuidResolver.apply(player);
                    return uuid != null && !repository.isVanished(uuid);
                })
                .collect(Collectors.toList());
    }
}
