package br.com.laboon.core.account.punishment;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.repository.PostgreSqlPunishmentRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class PunishmentService {

    private final AccountManager accountManager;
    private final PostgreSqlPunishmentRepository punishmentRepository;

    public PunishmentService(
            AccountManager accountManager,
            PostgreSqlPunishmentRepository punishmentRepository
    ) {

        if (accountManager == null) {
            throw new IllegalArgumentException(
                    "AccountManager não pode ser nulo."
            );
        }

        if (punishmentRepository == null) {
            throw new IllegalArgumentException(
                    "PostgreSqlPunishmentRepository não pode ser nulo."
            );
        }

        this.accountManager = accountManager;
        this.punishmentRepository = punishmentRepository;
    }

    /*
     * ============================================================
     * BAN
     * ============================================================
     */

    public Ban ban(
            Account target,
            String staffName,
            UUID staffUniqueId,
            String ip,
            String server,
            String reason
    ) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);

        PunishmentHistory history =
                target.getPunishmentHistory();

        Ban current =
                history.getCurrentBan();

        if (current != null) {
            throw new IllegalStateException(
                    "O jogador já está banido."
            );
        }

        Ban ban =
                new Ban(
                        staffName,
                        staffUniqueId,
                        ip,
                        server,
                        Instant.now(),
                        reason
                );

        /*
         * Primeiro adicionamos ao estado em memória.
         */
        history.addBan(ban);

        /*
         * Persistência definitiva.
         */
        punishmentRepository.saveBan(
                target.getUniqueId(),
                ban
        );

        /*
         * Atualiza PostgreSQL da Account + Redis.
         */
        accountManager.save(target);

        return ban;
    }

    /*
     * ============================================================
     * TEMP BAN
     * ============================================================
     */

    public Ban tempBan(
            Account target,
            String staffName,
            UUID staffUniqueId,
            String ip,
            String server,
            String reason,
            Duration duration
    ) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);
        validateDuration(duration);

        PunishmentHistory history =
                target.getPunishmentHistory();

        if (history.isBanned()) {
            throw new IllegalStateException(
                    "O jogador já está banido."
            );
        }

        Instant now =
                Instant.now();

        Ban ban =
                new Ban(
                        staffName,
                        staffUniqueId,
                        ip,
                        server,
                        now,
                        reason,
                        now.plus(duration)
                );

        history.addBan(ban);

        punishmentRepository.saveBan(
                target.getUniqueId(),
                ban
        );

        accountManager.save(target);

        return ban;
    }

    /*
     * ============================================================
     * UNBAN
     * ============================================================
     */

    public Ban unban(
            Account target,
            String staffName,
            UUID staffUniqueId
    ) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);

        Ban ban =
                target.getPunishmentHistory()
                        .getCurrentBan();

        if (ban == null) {
            throw new IllegalStateException(
                    "O jogador não está banido."
            );
        }

        /*
         * Altera o objeto em memória.
         */
        ban.unban(
                staffName,
                staffUniqueId
        );

        /*
         * Atualiza exatamente o registro
         * correspondente no PostgreSQL.
         */
        punishmentRepository.updateBan(
                target.getUniqueId(),
                ban
        );

        /*
         * Agora o Redis recebe o estado atualizado.
         */
        accountManager.save(target);

        return ban;
    }

    /*
     * ============================================================
     * MUTE
     * ============================================================
     */

    public Mute mute(
            Account target,
            String staffName,
            UUID staffUniqueId,
            String ip,
            String server,
            String reason
    ) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);

        PunishmentHistory history =
                target.getPunishmentHistory();

        if (history.isMuted()) {
            throw new IllegalStateException(
                    "O jogador já está mutado."
            );
        }

        Mute mute =
                new Mute(
                        staffName,
                        staffUniqueId,
                        ip,
                        server,
                        Instant.now(),
                        reason
                );

        history.addMute(mute);

        punishmentRepository.saveMute(
                target.getUniqueId(),
                mute
        );

        accountManager.save(target);

        return mute;
    }

    /*
     * ============================================================
     * TEMP MUTE
     * ============================================================
     */

    public Mute tempMute(
            Account target,
            String staffName,
            UUID staffUniqueId,
            String ip,
            String server,
            String reason,
            Duration duration
    ) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);
        validateDuration(duration);

        PunishmentHistory history =
                target.getPunishmentHistory();

        if (history.isMuted()) {
            throw new IllegalStateException(
                    "O jogador já está mutado."
            );
        }

        Instant now =
                Instant.now();

        Mute mute =
                new Mute(
                        staffName,
                        staffUniqueId,
                        ip,
                        server,
                        now,
                        reason,
                        now.plus(duration)
                );

        history.addMute(mute);

        punishmentRepository.saveMute(
                target.getUniqueId(),
                mute
        );

        accountManager.save(target);

        return mute;
    }

    /*
     * ============================================================
     * UNMUTE
     * ============================================================
     */

    public Mute unmute(
            Account target,
            String staffName,
            UUID staffUniqueId
    ) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);

        Mute mute =
                target.getPunishmentHistory()
                        .getCurrentMute();

        if (mute == null) {
            throw new IllegalStateException(
                    "O jogador não está mutado."
            );
        }

        mute.unmute(
                staffName,
                staffUniqueId
        );

        punishmentRepository.updateMute(
                target.getUniqueId(),
                mute
        );

        accountManager.save(target);

        return mute;
    }

    /*
     * ============================================================
     * KICK
     * ============================================================
     */

    public Kick kick(
            Account target,
            String staffName,
            UUID staffUniqueId,
            String server,
            String reason
    ) {

        validateTarget(target);
        validateStaff(staffName, staffUniqueId);
        validateReason(reason);

        Kick kick =
                new Kick(
                        staffName,
                        staffUniqueId,
                        server,
                        Instant.now(),
                        reason
                );

        target.getPunishmentHistory()
                .addKick(kick);

        punishmentRepository.saveKick(
                target.getUniqueId(),
                kick
        );

        accountManager.save(target);

        return kick;
    }

    /*
     * ============================================================
     * VALIDATION
     * ============================================================
     */

    private void validateTarget(
            Account target
    ) {

        if (target == null) {
            throw new IllegalArgumentException(
                    "Conta não encontrada."
            );
        }
    }

    private void validateStaff(
            String staffName,
            UUID staffUniqueId
    ) {

        if (staffName == null
                || staffName.isBlank()) {

            throw new IllegalArgumentException(
                    "Nome do responsável inválido."
            );
        }

        if (staffUniqueId == null) {

            throw new IllegalArgumentException(
                    "UUID do responsável inválido."
            );
        }
    }

    private void validateReason(
            String reason
    ) {

        if (reason == null
                || reason.isBlank()) {

            throw new IllegalArgumentException(
                    "O motivo não pode ser vazio."
            );
        }
    }

    private void validateDuration(
            Duration duration
    ) {

        if (duration == null
                || duration.isNegative()
                || duration.isZero()) {

            throw new IllegalArgumentException(
                    "Duração inválida."
            );
        }
    }
}