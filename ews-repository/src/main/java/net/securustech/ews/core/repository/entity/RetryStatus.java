package net.securustech.ews.core.repository.entity;

import lombok.Getter;

@Getter
public enum RetryStatus {

    SUCCESS("SUCCESS", 0),
    NEW("NEW", 1),
    IN_PROGRESS("IN-PROGRESS", 2),
    FAILED("FAILED", 3);

    private String status;
    private int statusCode;

    RetryStatus(String status, int statusCode) {

        this.status = status;
        this.statusCode = statusCode;
    }

    public static RetryStatus fromString(final String status) {

        return RetryStatus.valueOf(status);
    }

    @Override
    public String toString() {
        return status;
    }
}
