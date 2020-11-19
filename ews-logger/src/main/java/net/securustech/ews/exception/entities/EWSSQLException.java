package net.securustech.ews.exception.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

import static net.securustech.ews.exception.entities.EWSErrorCodeDefinitions.EWS_SQL_EXCEPTION;

@Component
@Getter
@Setter
public class EWSSQLException extends EWSException {

    private SQLException sqlException;

    public EWSSQLException() {
        super();
    }

    public EWSSQLException(String originalErrorMessage) {
        super(EWS_SQL_EXCEPTION.getEwsErrorCode(), originalErrorMessage);
    }

    public EWSSQLException(String originalErrorCode, String originalErrorMessage) {
        super(originalErrorCode, originalErrorMessage);
    }

    public EWSSQLException(String ewsErrorCode, String ewsErrorMessage, SQLException sqlException) {

        super(sqlException, ewsErrorCode, ewsErrorMessage);
        this.sqlException = sqlException;
    }
}
