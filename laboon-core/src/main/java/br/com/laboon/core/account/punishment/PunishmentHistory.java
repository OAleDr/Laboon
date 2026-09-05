package br.com.laboon.core.account.punishment;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PunishmentHistory {

    private final List<Ban> banHistory;
    private final List<Mute> muteHistory;
    private final List<Kick> kickHistory;

    public PunishmentHistory() {
        this.banHistory = new ArrayList<>();
        this.muteHistory = new ArrayList<>();
        this.kickHistory = new ArrayList<>();
    }

    public void addBan(Ban ban) {

        if (ban == null) {
            throw new IllegalArgumentException("Ban não pode ser nulo.");
        }

        banHistory.add(ban);
    }

    public void addMute(Mute mute) {

        if (mute == null) {
            throw new IllegalArgumentException("Mute não pode ser nulo.");
        }

        muteHistory.add(mute);
    }

    public void addKick(Kick kick) {

        if (kick == null) {
            throw new IllegalArgumentException("Kick não pode ser nulo.");
        }

        kickHistory.add(kick);
    }

    public Ban getCurrentBan() {

        for (int i = banHistory.size() - 1; i >= 0; i--) {

            Ban ban = banHistory.get(i);

            if (ban.isActive()) {
                return ban;
            }
        }

        return null;
    }

    public Mute getCurrentMute() {

        for (int i = muteHistory.size() - 1; i >= 0; i--) {

            Mute mute = muteHistory.get(i);

            if (mute.isActive()) {
                return mute;
            }
        }

        return null;
    }

    public boolean isBanned() {
        return getCurrentBan() != null;
    }

    public boolean isMuted() {
        return getCurrentMute() != null;
    }

    public List<Ban> getBanHistory() {
        return Collections.unmodifiableList(banHistory);
    }

    public List<Mute> getMuteHistory() {
        return Collections.unmodifiableList(muteHistory);
    }

    public List<Kick> getKickHistory() {
        return Collections.unmodifiableList(kickHistory);
    }

    public List<Kick> getRecentKicks(Duration period) {

        if (period == null || period.isNegative() || period.isZero()) {
            return List.of();
        }

        Instant minimum = Instant.now().minus(period);

        return kickHistory.stream().filter(kick -> !kick.getTime().isBefore(minimum)).toList();
    }

    public boolean shouldAutoban(Duration period, int kickLimit) {

        if (kickLimit <= 0) {
            return false;
        }

        return getRecentKicks(period).size() >= kickLimit;
    }
}