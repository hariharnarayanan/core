package net.securustech.ews.exception.response.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private String errorMessage;

    private String ewsErrorCode;
    private String ewsErrorMessage;

    private String downstreamErrorCode;
    private String downstreamErrorMessage;

    public ErrorResponse(String errorMessage, String ewsErrorCode, String ewsErrorMessage, String downstreamErrorCode, String downstreamErrorMessage) {

        this.errorMessage = errorMessage;
        this.ewsErrorCode = ewsErrorCode;
        this.ewsErrorMessage = ewsErrorMessage;
        this.downstreamErrorCode = downstreamErrorCode;
        this.downstreamErrorMessage = downstreamErrorMessage;
    }
}

