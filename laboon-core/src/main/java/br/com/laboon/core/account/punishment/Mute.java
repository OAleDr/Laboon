package br.com.laboon.core.account.punishment;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class Mute {

    private final String mutedBy;
    private final UUID mutedByUniqueId;

    private final String mutedIp;
    private final String server;

    private final Instant muteTime;
    private final String reason;

    private final Instant expire;

    private boolean unmuted;
    private String unmutedBy;
    private UUID unmutedByUniqueId;
    private Instant unmuteTime;

    public Mute(String mutedBy, UUID mutedByUniqueId, String mutedIp, String server, Instant muteTime, String reason) {
        this(mutedBy, mutedByUniqueId, mutedIp, server, muteTime, reason, null);
    }

    public Mute(String mutedBy, UUID mutedByUniqueId, String mutedIp, String server, Instant muteTime, String reason, Instant expire) {
        if (mutedBy == null || mutedBy.isBlank()) {
            throw new IllegalArgumentException("Responsável pelo mute não pode ser nulo.");
        }

        if (mutedByUniqueId == null) {
            throw new IllegalArgumentException("UUID do responsável não pode ser nulo.");
        }

        if (muteTime == null) {
            throw new IllegalArgumentException("Data do mute não pode ser nula.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Motivo do mute não pode ser vazio.");
        }

        if (expire != null && !expire.isAfter(muteTime)) {
            throw new IllegalArgumentException("Data de expiração inválida.");
        }

        this.mutedBy = mutedBy;
        this.mutedByUniqueId = mutedByUniqueId;
        this.mutedIp = mutedIp;
        this.server = server;
        this.muteTime = muteTime;
        this.reason = reason;
        this.expire = expire;
    }

    public String getMutedBy() {
        return mutedBy;
    }

    public UUID getMutedByUniqueId() {
        return mutedByUniqueId;
    }

    public String getMutedIp() {
        return mutedIp;
    }

    public String getServer() {
        return server;
    }

    public Instant getMuteTime() {
        return muteTime;
    }

    public String getReason() {
        return reason;
    }

    public Instant getExpire() {
        return expire;
    }

    public boolean isPermanent() {
        return expire == null;
    }

    public boolean hasExpired() {
        return expire != null && !Instant.now().isBefore(expire);
    }

    public boolean isUnmuted() {
        return unmuted;
    }

    public boolean isActive() {
        return !unmuted && !hasExpired();
    }

    public Duration getRemaining() {
        if (isPermanent()) {
            return null;
        }

        Duration remaining = Duration.between(Instant.now(), expire);

        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * Remove o mute utilizando o momento atual.
     * <p>
     * Usado quando um administrador executa /unmute.
     */
    public void unmute(String unmutedBy, UUID unmutedByUniqueId) {
        if (unmutedBy == null || unmutedBy.isBlank()) {
            throw new IllegalArgumentException("Responsável pelo unmute não pode ser nulo.");
        }

        if (unmutedByUniqueId == null) {
            throw new IllegalArgumentException("UUID do responsável não pode ser nulo.");
        }

        this.unmuted = true;
        this.unmutedBy = unmutedBy;
        this.unmutedByUniqueId = unmutedByUniqueId;
        this.unmuteTime = Instant.now();
    }

    /**
     * Restaura um unmute já existente no histórico.
     * <p>
     * Não utiliza Instant.now(), preservando o timestamp original
     * salvo no Redis.
     */
    public static Mute restore(String mutedBy, UUID mutedByUniqueId, String mutedIp, String server, Instant muteTime, String reason, Instant expire, boolean unmuted, String unmutedBy, UUID unmutedByUniqueId, Instant unmuteTime) {
        Mute mute = new Mute(mutedBy, mutedByUniqueId, mutedIp, server, muteTime, reason, expire);

        if (unmuted) {
            if (unmutedBy == null || unmutedBy.isBlank()) {
                throw new IllegalArgumentException("Responsável pelo unmute não pode ser nulo.");
            }

            if (unmutedByUniqueId == null) {
                throw new IllegalArgumentException("UUID do responsável pelo unmute não pode ser nulo.");
            }

            if (unmuteTime == null) {
                throw new IllegalArgumentException("Data do unmute não pode ser nula.");
            }

            mute.unmuted = true;
            mute.unmutedBy = unmutedBy;
            mute.unmutedByUniqueId = unmutedByUniqueId;
            mute.unmuteTime = unmuteTime;
        }

        return mute;
    }

    public String getUnmutedBy() {
        return unmutedBy;
    }

    public UUID getUnmutedByUniqueId() {
        return unmutedByUniqueId;
    }

    public Instant getUnmuteTime() {
        return unmuteTime;
    }
}