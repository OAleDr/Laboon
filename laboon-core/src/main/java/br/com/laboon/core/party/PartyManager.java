package br.com.laboon.core.party;

import br.com.laboon.core.redis.RedisManager;

import java.time.Instant;
import java.util.UUID;

public final class PartyManager {

    private static final long INVITE_DURATION_SECONDS = 60;

    private final PartyRepository repository;

    public PartyManager(RedisManager redisManager) {

        if (redisManager == null) {
            throw new IllegalArgumentException("RedisManager não pode ser nulo.");
        }

        this.repository = new PartyRepository(redisManager);
    }

    /*
     * =========================
     * PARTY
     * =========================
     */

    public Party create(UUID leader) {

        if (leader == null) {
            throw new IllegalArgumentException("Líder não pode ser nulo.");
        }

        if (hasParty(leader)) {
            throw new IllegalStateException("O jogador já possui uma Party.");
        }

        Party party = new Party(UUID.randomUUID(), leader);

        repository.save(party);

        return party;
    }

    public Party get(UUID partyId) {

        return repository.findById(partyId);
    }

    public Party getByMember(UUID uniqueId) {

        return repository.findByMember(uniqueId);
    }

    public boolean hasParty(UUID uniqueId) {

        return getByMember(uniqueId) != null;
    }

    public boolean isLeader(UUID uniqueId) {

        Party party = getByMember(uniqueId);

        return party != null && party.isLeader(uniqueId);
    }

    /*
     * =========================
     * MEMBERS
     * =========================
     */

    public boolean addMember(UUID partyId, UUID uniqueId) {

        if (partyId == null || uniqueId == null) {

            return false;
        }

        Party party = repository.findById(partyId);

        if (party == null) {
            return false;
        }

        if (party.containsMember(uniqueId)) {
            return false;
        }

        if (hasParty(uniqueId)) {
            return false;
        }

        if (!party.addMember(uniqueId)) {
            return false;
        }

        repository.save(party);

        return true;
    }

    public boolean removeMember(UUID partyId, UUID uniqueId) {

        if (partyId == null || uniqueId == null) {

            return false;
        }

        Party party = repository.findById(partyId);

        if (party == null) {
            return false;
        }

        if (!party.removeMember(uniqueId)) {
            return false;
        }

        repository.save(party);

        return true;
    }

    public boolean kickMember(UUID partyId, UUID uniqueId) {

        if (partyId == null || uniqueId == null) {

            return false;
        }

        Party party = repository.findById(partyId);

        if (party == null) {
            return false;
        }

        if (party.isLeader(uniqueId)) {
            return false;
        }

        if (!party.removeMember(uniqueId)) {
            return false;
        }

        repository.save(party);

        return true;
    }

    public boolean transferLeader(UUID partyId, UUID newLeader) {

        if (partyId == null || newLeader == null) {

            return false;
        }

        Party party = repository.findById(partyId);

        if (party == null) {
            return false;
        }

        if (!party.containsMember(newLeader)) {
            return false;
        }

        party.setLeader(newLeader);

        repository.save(party);

        return true;
    }

    public void disband(UUID partyId) {

        if (partyId == null) {
            return;
        }

        repository.delete(partyId);
    }

    /*
     * =========================
     * INVITES
     * =========================
     */

    public PartyInvite createInvite(UUID partyId, UUID inviter, UUID invited) {

        if (partyId == null) {
            throw new IllegalArgumentException("Party não pode ser nula.");
        }

        if (inviter == null) {
            throw new IllegalArgumentException("Convidador não pode ser nulo.");
        }

        if (invited == null) {
            throw new IllegalArgumentException("Convidado não pode ser nulo.");
        }

        if (inviter.equals(invited)) {
            throw new IllegalStateException("Você não pode convidar a si mesmo.");
        }

        Party party = repository.findById(partyId);

        if (party == null) {
            throw new IllegalStateException("Party não encontrada.");
        }

        if (!party.isLeader(inviter)) {
            throw new IllegalStateException("Somente o líder pode convidar jogadores.");
        }

        if (party.containsMember(invited)) {
            throw new IllegalStateException("O jogador já está na Party.");
        }

        if (hasParty(invited)) {
            throw new IllegalStateException("O jogador já pertence a uma Party.");
        }

        if (repository.hasInvite(invited)) {
            throw new IllegalStateException("O jogador já possui um convite pendente.");
        }

        Instant createdAt = Instant.now();

        Instant expiresAt = createdAt.plusSeconds(INVITE_DURATION_SECONDS);

        PartyInvite invite = new PartyInvite(partyId, inviter, invited, createdAt, expiresAt);

        repository.saveInvite(invite);

        return invite;
    }

    public PartyInvite getInvite(UUID invited) {

        return repository.findInvite(invited);
    }

    public boolean hasInvite(UUID invited) {

        return repository.hasInvite(invited);
    }

    public void removeInvite(UUID invited) {

        repository.deleteInvite(invited);
    }

    public boolean acceptInvite(UUID invited) {

        if (invited == null) {
            return false;
        }

        PartyInvite invite = repository.findInvite(invited);

        if (invite == null) {
            return false;
        }

        if (hasParty(invited)) {

            repository.deleteInvite(invited);

            return false;
        }

        Party party = repository.findById(invite.getPartyId());

        if (party == null) {

            repository.deleteInvite(invited);

            return false;
        }

        if (party.containsMember(invited)) {

            repository.deleteInvite(invited);

            return false;
        }

        if (!party.addMember(invited)) {
            return false;
        }

        repository.save(party);

        repository.deleteInvite(invited);

        return true;
    }

    public boolean declineInvite(UUID invited) {

        if (invited == null) {
            return false;
        }

        if (!repository.hasInvite(invited)) {
            return false;
        }

        repository.deleteInvite(invited);

        return true;
    }

    public PartyRepository getRepository() {
        return repository;
    }
}