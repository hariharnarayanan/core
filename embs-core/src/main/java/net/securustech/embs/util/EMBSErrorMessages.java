package net.securustech.embs.util;

import lombok.Getter;

@Getter
public enum EMBSErrorMessages {

    //99 == MicroService Port (Use 99 here for EMBS)
    //1 == EMBS Error
    //2 == External System Error
    //Last 2 Digits == Specific EMBS Error Code

    BROKER_UNREACHABLE("99101", "Underlying Message Broker seems to down or unreachable!!!"),
    SEND_TO_BROKER_FAILED("99102", "Sending Message to Underlying Broker Failed although Retried it several times!!!"),
    RECEIVE_FROM_BROKER_FAILED("99103", "Consuming Message from Underlying Broker Topic Failed although Retried it several times!!!");

    private String errorCode;
    private String errorMessage;

    EMBSErrorMessages(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
