package net.securustech.embs.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@Component
public class EMBSRetryableException extends Exception {

    private String embsErrorCode;
    private String embsErrorMessage;

    public EMBSRetryableException(String originalErrorMessage) {
        super(originalErrorMessage);
    }

    public EMBSRetryableException(String originalErrorMessage, String embsErrorCode, String embsErrorMessage) {
        super(originalErrorMessage + "\nEMBSError Code :-> " + embsErrorCode + "\nEMBS Error Message :-> " + embsErrorMessage);
        this.embsErrorCode = embsErrorCode;
        this.embsErrorMessage = embsErrorMessage;
    }
}
