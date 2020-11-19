package net.securustech.ews.logger;

import lombok.Getter;

@Getter
public enum LogLevel {
    TRACE("TRACE"),
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR"),
    NONE("");

    private String logLevel;

    LogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public static LogLevel fromString(final String logLevelString) {
        for (LogLevel logLevel : LogLevel.values()) {
            if (logLevel.logLevel.equalsIgnoreCase(logLevelString)) {
                return logLevel;
            }
        }

        return null;
    }
}
