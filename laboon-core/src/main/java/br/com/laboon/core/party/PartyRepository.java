package br.com.laboon.core.party;

import br.com.laboon.core.redis.RedisManager;

import redis.clients.jedis.JedisPooled;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PartyRepository {

    private static final String PARTY_KEY_PREFIX = "laboon:party:";

    private static final String MEMBER_KEY_PREFIX = "laboon:party:member:";

    private static final String INVITE_KEY_PREFIX = "laboon:party:invite:";

    private final RedisManager redisManager;

    public PartyRepository(RedisManager redisManager) {

        if (redisManager == null) {
            throw new IllegalArgumentException("RedisManager não pode ser nulo.");
        }

        this.redisManager = redisManager;
    }

    /*
     * =========================
     * PARTY
     * =========================
     */

    public void save(Party party) {

        if (party == null) {
            throw new IllegalArgumentException("Party não pode ser nula.");
        }

        JedisPooled jedis = redisManager.getJedis();

        String partyKey = PARTY_KEY_PREFIX + party.getUniqueId();

        /*
         * Recupera a Party anterior para
         * identificar membros que saíram.
         */
        Party previousParty = findById(party.getUniqueId());

        /*
         * Remove os índices dos membros
         * que não pertencem mais à Party.
         */
        if (previousParty != null) {

            for (PartyMember previousMember : previousParty.getMembers()) {

                UUID memberId = previousMember.getUniqueId();

                if (!party.containsMember(memberId)) {

                    String memberKey = MEMBER_KEY_PREFIX + memberId;

                    /*
                     * Só remove se o índice ainda
                     * apontar para esta Party.
                     */
                    String currentParty = jedis.get(memberKey);

                    if (party.getUniqueId().toString().equals(currentParty)) {

                        jedis.del(memberKey);
                    }
                }
            }
        }

        /*
         * Salva o líder.
         */
        jedis.hset(partyKey, "leader", party.getLeader().toString());

        /*
         * Salva a data de criação.
         */
        jedis.hset(partyKey, "createdAt", party.getCreatedAt().toString());

        /*
         * Salva os membros.
         */
        jedis.hset(partyKey, "members", serializeMembers(party));

        /*
         * Atualiza o índice dos membros atuais.
         */
        for (PartyMember member : party.getMembers()) {

            jedis.set(MEMBER_KEY_PREFIX + member.getUniqueId(), party.getUniqueId().toString());
        }
    }

    public Party findById(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        JedisPooled jedis = redisManager.getJedis();

        String partyKey = PARTY_KEY_PREFIX + uniqueId;

        if (!jedis.exists(partyKey)) {
            return null;
        }

        Map<String, String> data = jedis.hgetAll(partyKey);

        String leaderValue = data.get("leader");

        String createdAtValue = data.get("createdAt");

        String membersValue = data.get("members");

        if (leaderValue == null || createdAtValue == null || membersValue == null) {

            return null;
        }

        try {

            UUID leader = UUID.fromString(leaderValue);

            Instant createdAt = Instant.parse(createdAtValue);

            List<PartyMember> members = deserializeMembers(membersValue);

            return new Party(uniqueId, leader, createdAt, members);

        } catch (Exception exception) {

            return null;
        }
    }

    public Party findByMember(UUID uniqueId) {

        if (uniqueId == null) {
            return null;
        }

        JedisPooled jedis = redisManager.getJedis();

        String partyId = jedis.get(MEMBER_KEY_PREFIX + uniqueId);

        if (partyId == null || partyId.isBlank()) {

            return null;
        }

        try {

            UUID partyUuid = UUID.fromString(partyId);

            Party party = findById(partyUuid);

            /*
             * Se o índice existir mas a Party
             * não existir, limpa o índice.
             */
            if (party == null) {

                jedis.del(MEMBER_KEY_PREFIX + uniqueId);

                return null;
            }

            /*
             * Segurança adicional:
             * verifica se o jogador realmente
             * pertence à Party.
             */
            if (!party.containsMember(uniqueId)) {

                jedis.del(MEMBER_KEY_PREFIX + uniqueId);

                return null;
            }

            return party;

        } catch (IllegalArgumentException exception) {

            jedis.del(MEMBER_KEY_PREFIX + uniqueId);

            return null;
        }
    }

    public boolean exists(UUID partyId) {

        if (partyId == null) {
            return false;
        }

        JedisPooled jedis = redisManager.getJedis();

        return jedis.exists(PARTY_KEY_PREFIX + partyId);
    }

    public void delete(UUID partyId) {

        if (partyId == null) {
            return;
        }

        Party party = findById(partyId);

        JedisPooled jedis = redisManager.getJedis();

        /*
         * Remove o índice de todos os membros.
         */
        if (party != null) {

            for (PartyMember member : party.getMembers()) {

                String memberKey = MEMBER_KEY_PREFIX + member.getUniqueId();

                String currentParty = jedis.get(memberKey);

                if (partyId.toString().equals(currentParty)) {

                    jedis.del(memberKey);
                }
            }
        }

        /*
         * Remove a Party.
         */
        jedis.del(PARTY_KEY_PREFIX + partyId);
    }

    /*
     * =========================
     * INVITES
     * =========================
     */

    public void saveInvite(PartyInvite invite) {

        if (invite == null) {
            throw new IllegalArgumentException("Convite não pode ser nulo.");
        }

        JedisPooled jedis = redisManager.getJedis();

        String key = INVITE_KEY_PREFIX + invite.getInvited();

        jedis.hset(key, "partyId", invite.getPartyId().toString());

        jedis.hset(key, "inviter", invite.getInviter().toString());

        jedis.hset(key, "invited", invite.getInvited().toString());

        jedis.hset(key, "createdAt", invite.getCreatedAt().toString());

        jedis.hset(key, "expiresAt", invite.getExpiresAt().toString());

        long ttl = Math.max(1, Duration.between(Instant.now(), invite.getExpiresAt()).getSeconds());

        jedis.expire(key, ttl);
    }

    public PartyInvite findInvite(UUID invited) {

        if (invited == null) {
            return null;
        }

        JedisPooled jedis = redisManager.getJedis();

        String key = INVITE_KEY_PREFIX + invited;

        if (!jedis.exists(key)) {
            return null;
        }

        Map<String, String> data = jedis.hgetAll(key);

        String partyIdValue = data.get("partyId");

        String inviterValue = data.get("inviter");

        String invitedValue = data.get("invited");

        String createdAtValue = data.get("createdAt");

        String expiresAtValue = data.get("expiresAt");

        if (partyIdValue == null || inviterValue == null || invitedValue == null || createdAtValue == null || expiresAtValue == null) {

            deleteInvite(invited);

            return null;
        }

        try {

            UUID partyId = UUID.fromString(partyIdValue);

            UUID inviter = UUID.fromString(inviterValue);

            UUID invitedId = UUID.fromString(invitedValue);

            Instant createdAt = Instant.parse(createdAtValue);

            Instant expiresAt = Instant.parse(expiresAtValue);

            PartyInvite invite = new PartyInvite(partyId, inviter, invitedId, createdAt, expiresAt);

            /*
             * Verifica a expiração manualmente.
             */
            if (invite.isExpired()) {

                deleteInvite(invited);

                return null;
            }

            return invite;

        } catch (Exception exception) {

            deleteInvite(invited);

            return null;
        }
    }

    public boolean hasInvite(UUID invited) {

        if (invited == null) {
            return false;
        }

        return findInvite(invited) != null;
    }

    public void deleteInvite(UUID invited) {

        if (invited == null) {
            return;
        }

        JedisPooled jedis = redisManager.getJedis();

        jedis.del(INVITE_KEY_PREFIX + invited);
    }

    /*
     * =========================
     * SERIALIZAÇÃO
     * =========================
     */

    private String serializeMembers(Party party) {

        StringBuilder builder = new StringBuilder();

        for (PartyMember member : party.getMembers()) {

            if (!builder.isEmpty()) {
                builder.append(",");
            }

            builder.append(member.getUniqueId());

            builder.append(":");

            builder.append(member.getJoinedAt());
        }

        return builder.toString();
    }

    private List<PartyMember> deserializeMembers(String value) {

        List<PartyMember> members = new ArrayList<>();

        if (value == null || value.isBlank()) {

            return members;
        }

        String[] values = value.split(",");

        for (String memberValue : values) {

            int separator = memberValue.indexOf(":");

            if (separator <= 0 || separator >= memberValue.length() - 1) {

                continue;
            }

            try {

                UUID uniqueId = UUID.fromString(memberValue.substring(0, separator));

                Instant joinedAt = Instant.parse(memberValue.substring(separator + 1));

                members.add(new PartyMember(uniqueId, joinedAt));

            } catch (Exception ignored) {

                /*
                 * Ignora membros inválidos.
                 */
            }
        }

        return members;
    }
}