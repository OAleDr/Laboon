package br.com.laboon.core.party;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Party {

    private final UUID uniqueId;

    private UUID leader;

    private final Instant createdAt;

    private final List<PartyMember> members;

    public Party(UUID uniqueId, UUID leader) {

        if (uniqueId == null) {
            throw new IllegalArgumentException("UUID da Party não pode ser nulo.");
        }

        if (leader == null) {
            throw new IllegalArgumentException("Líder não pode ser nulo.");
        }

        this.uniqueId = uniqueId;
        this.leader = leader;
        this.createdAt = Instant.now();

        this.members = new ArrayList<>();

        members.add(new PartyMember(leader, createdAt));
    }

    public Party(UUID uniqueId, UUID leader, Instant createdAt, List<PartyMember> members) {

        if (uniqueId == null) {
            throw new IllegalArgumentException("UUID da Party não pode ser nulo.");
        }

        if (leader == null) {
            throw new IllegalArgumentException("Líder não pode ser nulo.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Data de criação não pode ser nula.");
        }

        this.uniqueId = uniqueId;
        this.leader = leader;
        this.createdAt = createdAt;

        this.members = new ArrayList<>();

        if (members != null) {
            this.members.addAll(members);
        }

        /*
         * Garante que o líder também seja
         * membro da Party.
         */
        if (!containsMember(leader)) {

            this.members.add(new PartyMember(leader, createdAt));
        }
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {

        if (leader == null) {
            throw new IllegalArgumentException("Líder não pode ser nulo.");
        }

        if (!containsMember(leader)) {
            throw new IllegalArgumentException("O novo líder precisa ser membro da Party.");
        }

        this.leader = leader;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PartyMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public boolean containsMember(UUID uniqueId) {

        if (uniqueId == null) {
            return false;
        }

        return members.stream().anyMatch(member -> member.getUniqueId().equals(uniqueId));
    }

    public PartyMember getMember(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        return members.stream().filter(member -> member.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public boolean addMember(UUID uniqueId) {

        if (uniqueId == null) {
            return false;
        }

        if (containsMember(uniqueId)) {
            return false;
        }

        members.add(new PartyMember(uniqueId, Instant.now()));

        return true;
    }

    public boolean removeMember(UUID uniqueId) {

        if (uniqueId == null) {
            return false;
        }

        /*
         * O líder não pode sair diretamente.
         */
        if (uniqueId.equals(leader)) {
            return false;
        }

        return members.removeIf(member -> member.getUniqueId().equals(uniqueId));
    }

    public int getMemberCount() {
        return members.size();
    }

    public boolean isLeader(UUID uniqueId) {

        return uniqueId != null && uniqueId.equals(leader);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }
}