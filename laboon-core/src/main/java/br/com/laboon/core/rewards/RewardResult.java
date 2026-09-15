package br.com.laboon.core.rewards;

import java.util.Collections;
import java.util.List;

public final class RewardResult {

    public enum Status {

        SUCCESS,
        INVALID_PLAYER,
        EMPTY_REWARD,
        FAILED
    }

    private final Status status;
    private final List<Reward> rewards;

    private RewardResult(
            Status status,
            List<Reward> rewards
    ) {

        this.status = status;
        this.rewards = rewards;
    }

    public static RewardResult success(
            List<Reward> rewards
    ) {

        return new RewardResult(
                Status.SUCCESS,
                rewards
        );
    }

    public static RewardResult failure(
            Status status
    ) {

        return new RewardResult(
                status,
                Collections.emptyList()
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}