package br.com.laboon.core.rewards;

import java.util.Collections;
import java.util.List;

public final class RewardResult {

    public enum Status {

        SUCCESS,

        ALREADY_CLAIMED,

        INVALID_PLAYER,

        EMPTY_REWARD,

        INVALID_REWARD_ID,

        FAILED
    }

    private final Status status;
    private final List<Reward> rewards;

    private RewardResult(
            Status status,
            List<Reward> rewards
    ) {

        this.status = status;

        this.rewards =
                rewards == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                        rewards
                );
    }

    public static RewardResult success(
            List<Reward> rewards
    ) {

        return new RewardResult(
                Status.SUCCESS,
                rewards
        );
    }

    public static RewardResult alreadyClaimed() {

        return new RewardResult(
                Status.ALREADY_CLAIMED,
                Collections.emptyList()
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

    public boolean isAlreadyClaimed() {
        return status == Status.ALREADY_CLAIMED;
    }
}