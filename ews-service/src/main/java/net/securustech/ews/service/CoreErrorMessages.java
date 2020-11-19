package net.securustech.ews.service;

import lombok.Getter;

@Getter
public enum CoreErrorMessages {

    EXTERNAL_SYSTEM_RETRY_LOG_SAVE_FAILED("90009", "Saving External System Retry Logging Failed"),
    EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND("90010", "External System Retry Log Data not found for provided criteria"),
    EXTERNAL_SYSTEM_RETRY_LOG_UNABLE_TO_MODIFY("90011", "External System Retry Log Data could not be modified for provided criteria and input"),
    EXTERNAL_SYSTEM_REQUEST_FAILED("90012", "Request to External System could not be sent for provided criteria and input");

    private String errorCode;
    private String errorMessage;

    CoreErrorMessages(String errorCode, String errorMessage) {

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
