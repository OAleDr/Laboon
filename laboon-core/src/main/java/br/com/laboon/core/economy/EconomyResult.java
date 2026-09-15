package br.com.laboon.core.economy;

public final class EconomyResult {

    public enum Status {
        SUCCESS,
        INVALID_AMOUNT,
        INSUFFICIENT_FUNDS,
        ACCOUNT_NOT_FOUND,
        ERROR
    }

    private final Status status;
    private final long balance;

    private EconomyResult(
            Status status,
            long balance
    ) {
        this.status = status;
        this.balance = balance;
    }

    public static EconomyResult success(long balance) {
        return new EconomyResult(
                Status.SUCCESS,
                balance
        );
    }

    public static EconomyResult failure(Status status) {
        return new EconomyResult(
                status,
                0L
        );
    }

    public Status getStatus() {
        return status;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}