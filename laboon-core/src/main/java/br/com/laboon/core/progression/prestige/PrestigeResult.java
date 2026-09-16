package br.com.laboon.core.progression.prestige;

import br.com.laboon.core.progression.ProgressionSnapshot;

public final class PrestigeResult {
    public enum Status {
        SUCCESS, INVALID_PLAYER, MAX_PRESTIGE, REQUIREMENT_NOT_MET, FAILED
    }

    private final Status status;
    private final Prestige prestige;
    private final ProgressionSnapshot snapshot;

    private PrestigeResult(Status status, Prestige prestige, ProgressionSnapshot snapshot) {
        this.status = status;
        this.prestige = prestige;
        this.snapshot = snapshot;
    }

    public static PrestigeResult success(Prestige prestige, ProgressionSnapshot snapshot) {
        return new PrestigeResult(Status.SUCCESS, prestige, snapshot);
    }

    public static PrestigeResult failure(Status status) {
        return new PrestigeResult(status, null, null);
    }

    public Status getStatus() { return status; }
    public Prestige getPrestige() { return prestige; }
    public ProgressionSnapshot getSnapshot() { return snapshot; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
}
