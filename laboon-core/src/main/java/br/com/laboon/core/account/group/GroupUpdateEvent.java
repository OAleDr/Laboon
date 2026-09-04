package br.com.laboon.core.account.group;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class GroupUpdateEvent {

    public static final String CHANNEL = "laboon:group:update";

    private final UUID uniqueId;
    private final Group permanentGroup;
    private final Map<Group, Instant> temporaryGroups;

    public GroupUpdateEvent(UUID uniqueId, Group permanentGroup, Map<Group, Instant> temporaryGroups) {

        if (uniqueId == null) {
            throw new IllegalArgumentException("UUID não pode ser nulo.");
        }

        if (permanentGroup == null) {
            throw new IllegalArgumentException("Grupo permanente não pode ser nulo.");
        }

        this.uniqueId = uniqueId;
        this.permanentGroup = permanentGroup;
        this.temporaryGroups = temporaryGroups == null ? Map.of() : Map.copyOf(temporaryGroups);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public Group getPermanentGroup() {
        return permanentGroup;
    }

    public Map<Group, Instant> getTemporaryGroups() {
        return temporaryGroups;
    }
}