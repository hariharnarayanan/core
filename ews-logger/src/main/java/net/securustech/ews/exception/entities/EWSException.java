package net.securustech.ews.exception.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@Getter
@Setter
public class EWSException extends Exception {

    private String ewsErrorCode;
    private String ewsErrorMessage;

    private String downstreamErrorCode;
    private String downstreamErrorMessage;

    private String sourceType;
    private String httpMethod;
    private String sourceName;
    private String methodName;
    private String uuid;
    private Long duration;

    public EWSException(Throwable cause) {

        super(cause);
    }

    public EWSException(String ewsErrorCode, String ewsErrorMessage) {

        this.ewsErrorCode = ewsErrorCode;
        this.ewsErrorMessage = ewsErrorMessage;
    }

    public EWSException(Throwable cause, String ewsErrorCode, String ewsErrorMessage) {

        super(cause);
        this.ewsErrorCode = ewsErrorCode;
        this.ewsErrorMessage = ewsErrorMessage;
    }

    public EWSException(String ewsErrorCode, String ewsErrorMessage, String downstreamErrorCode, String downstreamErrorMessage) {

        this.ewsErrorCode = ewsErrorCode;
        this.ewsErrorMessage = ewsErrorMessage;
        this.downstreamErrorCode = downstreamErrorCode;
        this.downstreamErrorMessage = downstreamErrorMessage;
    }

    public EWSException(Throwable cause, String ewsErrorCode, String ewsErrorMessage, String downstreamErrorCode, String downstreamErrorMessage) {

        super(cause);
        this.ewsErrorCode = ewsErrorCode;
        this.ewsErrorMessage = ewsErrorMessage;
        this.downstreamErrorCode = downstreamErrorCode;
        this.downstreamErrorMessage = downstreamErrorMessage;
    }

    public EWSException(String ewsErrorCode, String ewsErrorMessage, String downstreamErrorCode, String downstreamErrorMessage,
                        String sourceType, String httpMethod, String sourceName, String methodName, String uuid, Long duration) {

        this.ewsErrorCode = ewsErrorCode;
        this.ewsErrorMessage = ewsErrorMessage;
        this.downstreamErrorCode = downstreamErrorCode;
        this.downstreamErrorMessage = downstreamErrorMessage;
        this.sourceType = sourceType;
        this.httpMethod = httpMethod;
        this.sourceName = sourceName;
        this.methodName = methodName;
        this.uuid = uuid;
        this.duration = duration;

    }

    public EWSException(Throwable cause, String ewsErrorCode, String ewsErrorMessage, String downstreamErrorCode, String downstreamErrorMessage,
                        String sourceType, String httpMethod, String sourceName, String methodName, String uuid, Long duration) {

        super(cause);
        this.ewsErrorCode = ewsErrorCode;
        this.ewsErrorMessage = ewsErrorMessage;
        this.downstreamErrorCode = downstreamErrorCode;
        this.downstreamErrorMessage = downstreamErrorMessage;
        this.sourceType = sourceType;
        this.httpMethod = httpMethod;
        this.sourceName = sourceName;
        this.methodName = methodName;
        this.uuid = uuid;
        this.duration = duration;

    }
}
