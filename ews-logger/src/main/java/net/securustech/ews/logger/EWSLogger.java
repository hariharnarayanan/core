package net.securustech.ews.logger;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EWSLogger {

    String name() default "";

    SourceType type() default SourceType.NONE;

    LogLevel logLevel() default LogLevel.INFO;

    String httpMethod() default "NONE";

    boolean logRequest() default false;

    boolean logResults() default false;

}
