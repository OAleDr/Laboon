package br.com.laboon.core.account.punishment;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class Ban {

    private final String bannedBy;
    private final UUID bannedByUniqueId;

    private final String bannedIp;
    private final String server;

    private final Instant banTime;
    private final String reason;

    private final Instant expire;

    private boolean unbanned;
    private String unbannedBy;
    private UUID unbannedByUniqueId;
    private Instant unbanTime;

    public Ban(String bannedBy, UUID bannedByUniqueId, String bannedIp, String server, Instant banTime, String reason) {
        this(bannedBy, bannedByUniqueId, bannedIp, server, banTime, reason, null);
    }

    public Ban(String bannedBy, UUID bannedByUniqueId, String bannedIp, String server, Instant banTime, String reason, Instant expire) {
        if (bannedBy == null || bannedBy.isBlank()) {
            throw new IllegalArgumentException("Responsável pelo ban não pode ser nulo.");
        }

        if (bannedByUniqueId == null) {
            throw new IllegalArgumentException("UUID do responsável não pode ser nulo.");
        }

        if (banTime == null) {
            throw new IllegalArgumentException("Data do ban não pode ser nula.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Motivo do ban não pode ser vazio.");
        }

        if (expire != null && !expire.isAfter(banTime)) {
            throw new IllegalArgumentException("Data de expiração inválida.");
        }

        this.bannedBy = bannedBy;
        this.bannedByUniqueId = bannedByUniqueId;
        this.bannedIp = bannedIp;
        this.server = server;
        this.banTime = banTime;
        this.reason = reason;
        this.expire = expire;
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public UUID getBannedByUniqueId() {
        return bannedByUniqueId;
    }

    public String getBannedIp() {
        return bannedIp;
    }

    public String getServer() {
        return server;
    }

    public Instant getBanTime() {
        return banTime;
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

    public boolean isUnbanned() {
        return unbanned;
    }

    public boolean isActive() {
        return !unbanned && !hasExpired();
    }

    public Duration getRemaining() {
        if (isPermanent()) {
            return null;
        }

        Duration remaining = Duration.between(Instant.now(), expire);

        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * Remove o ban utilizando o momento atual.
     * <p>
     * Usado quando um administrador executa /unban.
     */
    public void unban(String unbannedBy, UUID unbannedByUniqueId) {
        if (unbannedBy == null || unbannedBy.isBlank()) {
            throw new IllegalArgumentException("Responsável pelo unban não pode ser nulo.");
        }

        if (unbannedByUniqueId == null) {
            throw new IllegalArgumentException("UUID do responsável não pode ser nulo.");
        }

        this.unbanned = true;
        this.unbannedBy = unbannedBy;
        this.unbannedByUniqueId = unbannedByUniqueId;
        this.unbanTime = Instant.now();
    }

    /**
     * Restaura um unban já existente no histórico.
     * <p>
     * Não utiliza Instant.now(), preservando o timestamp original
     * salvo no Redis.
     */
    public static Ban restore(String bannedBy, UUID bannedByUniqueId, String bannedIp, String server, Instant banTime, String reason, Instant expire, boolean unbanned, String unbannedBy, UUID unbannedByUniqueId, Instant unbanTime) {
        Ban ban = new Ban(bannedBy, bannedByUniqueId, bannedIp, server, banTime, reason, expire);

        if (unbanned) {
            if (unbannedBy == null || unbannedBy.isBlank()) {
                throw new IllegalArgumentException("Responsável pelo unban não pode ser nulo.");
            }

            if (unbannedByUniqueId == null) {
                throw new IllegalArgumentException("UUID do responsável pelo unban não pode ser nulo.");
            }

            if (unbanTime == null) {
                throw new IllegalArgumentException("Data do unban não pode ser nula.");
            }

            ban.unbanned = true;
            ban.unbannedBy = unbannedBy;
            ban.unbannedByUniqueId = unbannedByUniqueId;
            ban.unbanTime = unbanTime;
        }

        return ban;
    }

    public String getUnbannedBy() {
        return unbannedBy;
    }

    public UUID getUnbannedByUniqueId() {
        return unbannedByUniqueId;
    }

    public Instant getUnbanTime() {
        return unbanTime;
    }
}