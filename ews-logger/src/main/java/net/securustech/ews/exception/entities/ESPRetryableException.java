package net.securustech.ews.exception.entities;

import org.springframework.stereotype.Component;

@Component
public class ESPRetryableException extends Exception {

    private String espErrorCode;
    private String espErrorMessage;

    public ESPRetryableException() {
    }

    public ESPRetryableException(String originalErrorMessage) {
        super(originalErrorMessage);
    }

    public ESPRetryableException(String originalErrorMessage, String espErrorCode, String espErrorMessage) {
        super(originalErrorMessage + "\nESPError Code :-> " + espErrorCode + "\nESP Error Message :-> " + espErrorMessage);
        this.espErrorCode = espErrorCode;
        this.espErrorMessage = espErrorMessage;
    }

    public String getEspErrorCode() {
        return this.espErrorCode;
    }

    public String getEspErrorMessage() {
        return this.espErrorMessage;
    }

    public void setEspErrorCode(String espErrorCode) {
        this.espErrorCode = espErrorCode;
    }

    public void setEspErrorMessage(String espErrorMessage) {
        this.espErrorMessage = espErrorMessage;
    }
}
