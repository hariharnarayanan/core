package net.securustech.ews.exception.entities;

import org.springframework.stereotype.Component;

@Component
public class EWSRetryableException extends Exception {

    private String ewsErrorCode;
    private String ewsErrorMessage;

    public EWSRetryableException() {
    }

    public EWSRetryableException(String originalErrorMessage) {
        super(originalErrorMessage);
    }

    public EWSRetryableException(String originalErrorMessage, String ewsErrorCode, String ewsErrorMessage) {
        super(originalErrorMessage + "\nESPError Code :-> " + ewsErrorCode + "\nESP Error Message :-> " + ewsErrorMessage);
        this.ewsErrorCode = ewsErrorCode;
        this.ewsErrorMessage = ewsErrorMessage;
    }

    public String getEwsErrorCode() {
        return this.ewsErrorCode;
    }

    public String getEwsErrorMessage() {
        return this.ewsErrorMessage;
    }

    public void setEwsErrorCode(String ewsErrorCode) {
        this.ewsErrorCode = ewsErrorCode;
    }

    public void setEwsErrorMessage(String ewsErrorMessage) {
        this.ewsErrorMessage = ewsErrorMessage;
    }
}
