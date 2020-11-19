package net.securustech.embs.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@Component
public class EMBSNonRetryableException extends Exception {

    private String embsErrorCode;
    private String embsErrorMessage;

    public EMBSNonRetryableException(String originalErrorMessage) {
        super(originalErrorMessage);
    }

    public EMBSNonRetryableException(String originalErrorMessage, String embsErrorCode, String embsErrorMessage) {
        super(originalErrorMessage + "\nEMBSError Code :-> " + embsErrorCode + "\nEMBS Error Message :-> " + embsErrorMessage);
        this.embsErrorCode = embsErrorCode;
        this.embsErrorMessage = embsErrorMessage;
    }
}
