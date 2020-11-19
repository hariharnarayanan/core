package net.securustech.ews.exception.entities;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static net.securustech.ews.exception.entities.EWSErrorCodeDefinitions.EWS_BAD_REQUEST_EXCEPTION;

@Component
public class EWSBadRequestException extends EWSException {

    public EWSBadRequestException() {
        super();
    }

    public EWSBadRequestException(String originalErrorMessage) {
        super(EWS_BAD_REQUEST_EXCEPTION.getEwsErrorCode(), originalErrorMessage);
    }

    public EWSBadRequestException(String originalErrorMessage, String ewsErrorCode, String ewsErrorMessage) {
        super(ewsErrorCode, ewsErrorMessage, EWS_BAD_REQUEST_EXCEPTION.getEwsErrorCode(), originalErrorMessage);
    }

    public EWSBadRequestException(String ewsErrorCode, String ewsErrorMessage, String downstreamErrorCode, String downstreamErrorMessage) {
        super(ewsErrorCode, ewsErrorMessage, downstreamErrorCode, downstreamErrorMessage);
    }

    public EWSBadRequestException(String ewsErrorCode, String ewsErrorMessage, String downstreamErrorCode, String downstreamErrorMessage,
                        String sourceType, String httpMethod, String sourceName, String methodName, String uuid, Long duration) {
        super(ewsErrorCode, ewsErrorMessage, downstreamErrorCode, downstreamErrorMessage,
                sourceType,httpMethod,sourceName,methodName,uuid,duration);
    }

    @Override
    public String getEwsErrorCode() {
        if(!StringUtils.hasText(super.getEwsErrorCode())) {
            return EWS_BAD_REQUEST_EXCEPTION.getEwsErrorCode();
        }
        return super.getEwsErrorCode();
    }

    @Override
    public String getEwsErrorMessage() {
        if(!StringUtils.hasText(super.getEwsErrorMessage())) {
            return EWS_BAD_REQUEST_EXCEPTION.getEwsErrorMessage();
        }
        return super.getEwsErrorMessage();
    }
}
