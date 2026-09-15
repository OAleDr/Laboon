package br.com.laboon.core.progression;

import java.util.Collections;
import java.util.List;

public final class ProgressionResult {

    public enum Status {

        SUCCESS,
        NO_CHANGE,
        INVALID_PLAYER,
        INVALID_AMOUNT,
        INVALID_GAME,
        FAILED
    }

    private final Status status;
    private final ProgressionSnapshot snapshot;
    private final List<Integer> levelsGained;

    private ProgressionResult(
            Status status,
            ProgressionSnapshot snapshot,
            List<Integer> levelsGained
    ) {
        this.status = status;
        this.snapshot = snapshot;

        this.levelsGained =
                levelsGained == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                                levelsGained
                        );
    }

    public static ProgressionResult success(
            ProgressionSnapshot snapshot,
            List<Integer> levelsGained
    ) {
        return new ProgressionResult(
                Status.SUCCESS,
                snapshot,
                levelsGained
        );
    }

    public static ProgressionResult noChange(
            ProgressionSnapshot snapshot
    ) {
        return new ProgressionResult(
                Status.NO_CHANGE,
                snapshot,
                Collections.emptyList()
        );
    }

    public static ProgressionResult failure(
            Status status
    ) {
        return new ProgressionResult(
                status,
                null,
                Collections.emptyList()
        );
    }

    public Status getStatus() {
        return status;
    }

    public ProgressionSnapshot getSnapshot() {
        return snapshot;
    }

    public List<Integer> getLevelsGained() {
        return levelsGained;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isNoChange() {
        return status == Status.NO_CHANGE;
    }
}
