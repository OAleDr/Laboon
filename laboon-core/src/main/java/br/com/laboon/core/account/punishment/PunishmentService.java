package br.com.laboon.core.account.punishment;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class PunishmentService {

    private final AccountManager accountManager;

    public PunishmentService(AccountManager accountManager) {

        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        this.accountManager = accountManager;
    }

    public Ban ban(Account target, String staffName, UUID staffUniqueId, String ip, String server, String reason) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);

        PunishmentHistory history = target.getPunishmentHistory();

        Ban current = history.getCurrentBan();

        if (current != null) {
            throw new IllegalStateException("O jogador já está banido.");
        }

        Ban ban = new Ban(staffName, staffUniqueId, ip, server, Instant.now(), reason);

        history.addBan(ban);

        save(target);

        return ban;
    }

    public Ban tempBan(Account target, String staffName, UUID staffUniqueId, String ip, String server, String reason, Duration duration) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);
        validateDuration(duration);

        PunishmentHistory history = target.getPunishmentHistory();

        if (history.isBanned()) {
            throw new IllegalStateException("O jogador já está banido.");
        }

        Instant now = Instant.now();

        Ban ban = new Ban(staffName, staffUniqueId, ip, server, now, reason, now.plus(duration));

        history.addBan(ban);

        save(target);

        return ban;
    }

    public Ban unban(Account target, String staffName, UUID staffUniqueId) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);

        Ban ban = target.getPunishmentHistory().getCurrentBan();

        if (ban == null) {
            throw new IllegalStateException("O jogador não está banido.");
        }

        ban.unban(staffName, staffUniqueId);

        save(target);

        return ban;
    }

    public Mute mute(Account target, String staffName, UUID staffUniqueId, String ip, String server, String reason) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);

        PunishmentHistory history = target.getPunishmentHistory();

        if (history.isMuted()) {
            throw new IllegalStateException("O jogador já está mutado.");
        }

        Mute mute = new Mute(staffName, staffUniqueId, ip, server, Instant.now(), reason);

        history.addMute(mute);

        save(target);

        return mute;
    }

    public Mute tempMute(Account target, String staffName, UUID staffUniqueId, String ip, String server, String reason, Duration duration) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);
        validateDuration(duration);

        PunishmentHistory history = target.getPunishmentHistory();

        if (history.isMuted()) {
            throw new IllegalStateException("O jogador já está mutado.");
        }

        Instant now = Instant.now();

        Mute mute = new Mute(staffName, staffUniqueId, ip, server, now, reason, now.plus(duration));

        history.addMute(mute);

        save(target);

        return mute;
    }

    public Mute unmute(Account target, String staffName, UUID staffUniqueId) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);

        Mute mute = target.getPunishmentHistory().getCurrentMute();

        if (mute == null) {
            throw new IllegalStateException("O jogador não está mutado.");
        }

        mute.unmute(staffName, staffUniqueId);

        save(target);

        return mute;
    }

    public Kick kick(Account target, String staffName, UUID staffUniqueId, String server, String reason) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);

        Kick kick = new Kick(staffName, staffUniqueId, server, Instant.now(), reason);

        target.getPunishmentHistory().addKick(kick);

        save(target);

        return kick;
    }

    private void save(Account account) {
        accountManager.save(account);
    }

    private void validateTarget(Account target) {

        if (target == null) {
            throw new IllegalArgumentException("Conta não encontrada.");
        }
    }

    private void validateStaff(String staffName, UUID staffUniqueId) {

        if (staffName == null || staffName.isBlank()) {
            throw new IllegalArgumentException("Nome do responsável inválido.");
        }

        if (staffUniqueId == null) {
            throw new IllegalArgumentException("UUID do responsável inválido.");
        }
    }

    private void validateReason(String reason) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("O motivo não pode ser vazio.");
        }
    }

    private void validateDuration(Duration duration) {

        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Duração inválida.");
        }
    }
}