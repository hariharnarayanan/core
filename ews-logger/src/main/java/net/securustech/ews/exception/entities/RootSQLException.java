package net.securustech.ews.exception.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RootSQLException {

    int errorCode = 0;
    String sqlState = null;
    String message = null;

    public RootSQLException(int errorCode, String sqlState, String message) {

        this.errorCode = errorCode;
        this.sqlState = sqlState;
        this.message = message;
    }
}
