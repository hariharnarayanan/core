package net.securustech.ews.exception.handler;

import net.securustech.ews.exception.entities.EWSSQLException;
import net.securustech.ews.exception.entities.RootSQLException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class RootSQLExceptionBuilder {

    public RootSQLException buildRootSQLException(SQLException sqle) {

        int errorCode = 0;
        String sqlState = null;
        StringBuffer message = new StringBuffer();

        while (sqle != null) {

            sqlState = sqle.getSQLState();
            errorCode = sqle.getErrorCode();
            message.append(sqle.getMessage() + " : ");

            sqle = sqle.getNextException();
        }

        return new RootSQLException(errorCode, sqlState, message.toString());
    }

    public RootSQLException buildRootSQLException(EWSSQLException e) {

        RootSQLException rootSQLException = buildRootSQLException(e.getSqlException());

        if (StringUtils.isBlank(rootSQLException.getMessage())) {

            rootSQLException.setMessage(e.getMessage());
        }

        return new RootSQLException(rootSQLException.getErrorCode(), rootSQLException.getSqlState(), rootSQLException.getMessage());
    }
}