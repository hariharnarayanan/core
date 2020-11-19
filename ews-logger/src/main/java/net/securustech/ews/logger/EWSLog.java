package net.securustech.ews.logger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EWSLog {
    private String sourceType;
    private String httpMethod;
    private String sourceName;
    private String methodName;
    private Long executionStartTime;

}
