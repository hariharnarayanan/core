package net.securustech.ews.exception.handler;


import net.securustech.embs.util.CorrelationId;
import net.securustech.ews.exception.entities.EWSBadRequestException;
import net.securustech.ews.exception.entities.EWSException;
import net.securustech.ews.exception.entities.EWSSQLException;
import net.securustech.ews.exception.entities.RootSQLException;
import net.securustech.ews.exception.response.entities.ErrorResponse;
import net.securustech.ews.logger.EWSLog;
import net.securustech.ews.logger.EWSLogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;

import static net.securustech.ews.exception.entities.EWSErrorCodeDefinitions.EWS_RUN_TIME_ERROR;
import static net.securustech.ews.exception.entities.EWSErrorCodeDefinitions.EWS_SQL_EXCEPTION;

@ControllerAdvice
@RestController
@ComponentScan(value = "net.securustech.ews.exception")
@Order()
public class EWSExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private RootSQLExceptionBuilder rootSQLExceptionBuilder;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(value = {EWSBadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(EWSBadRequestException e, HttpServletRequest request) {

        return buildEWSErrorResponse(HttpStatus.BAD_REQUEST, e, request);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(value = {EWSSQLException.class})
    public ResponseEntity<ErrorResponse> handleEWSSQLException(EWSSQLException e, HttpServletRequest request) {

        RootSQLException rootSQLException = rootSQLExceptionBuilder.buildRootSQLException(e);

        EWSException ewse = new EWSException(e, e.getEwsErrorCode(),
                e.getEwsErrorMessage() + " : SQLState :-> " + rootSQLException.getSqlState() + " : SQLErrorCode :-> " + rootSQLException.getErrorCode() + " : SQLErrorMessage :-> " + rootSQLException.getMessage());

        return buildEWSErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ewse, request);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(EWSException.class)
    public ResponseEntity<ErrorResponse> handleEWSException(EWSException e, HttpServletRequest request) {
        return buildEWSErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request);
    }



    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(value = {SQLException.class})
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException e, HttpServletRequest request) {

        RootSQLException rootSQLException = rootSQLExceptionBuilder.buildRootSQLException(e);

        EWSException ewse = new EWSException(e, EWS_SQL_EXCEPTION.getEwsErrorCode(),
                EWS_SQL_EXCEPTION.getEwsErrorMessage() + " : SQLState :-> " + rootSQLException.getSqlState() + " : SQLErrorCode :-> " + rootSQLException.getErrorCode() + " : SQLErrorMessage :-> " + rootSQLException.getMessage());

        return buildEWSErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ewse, request);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(Exception e, HttpServletRequest request) {

       return buildEWSErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                new EWSException(e,EWS_RUN_TIME_ERROR.getEwsErrorMessage(), EWS_RUN_TIME_ERROR.getEwsErrorCode()), request);
    }

    private void logException(final HttpStatus httpStatus, final ErrorResponse er, final Exception e) {

        StringBuilder exceptionMessage = new StringBuilder("");

        if (er.getEwsErrorCode() != null || er.getEwsErrorMessage() != null) {
            exceptionMessage.append(
                    "EWSException <<::>> EWSErrorCode :-> " + er.getEwsErrorCode()
                            + " : EWSErrorMessage :-> " + er.getEwsErrorMessage());
        }
        if (er.getDownstreamErrorCode() != null || er.getDownstreamErrorMessage() != null) {
            exceptionMessage.append("EWSException <<::>> DownstreamErrorCode :-> "
                    + er.getDownstreamErrorCode() + " : DownstreamErrorMessage :-> ");
        }

        LOGGER.error("HttpStatus :-> " + httpStatus + " <<::>> " + exceptionMessage.toString() + " <<::>> : ErrorMessage :-> ", e);

    }

    private ResponseEntity<ErrorResponse> buildEWSErrorResponse(HttpStatus httpStatus, EWSException e, HttpServletRequest request) {
        String ewsErrorCode = null;
        String ewsErrorMessage = null;
        String downstreamErrorCode = null;
        String downstreamErrorMessage = null;
        String userName = "NA";
        String applicationName = "NA";
        String migrationIndicator = "NA";
        String httpMethod = "NONE";
        String sourceType = "NA";
        String sourceName = "NA";
        String methodName = "NA";
        EWSLog ewsLog = EWSLogContext.getEWSLog();
        Long duration = 0L;
        if (ewsLog != null) {
            sourceType = ewsLog.getSourceType();
            sourceName = ewsLog.getSourceName();
            httpMethod = ewsLog.getHttpMethod();
            methodName = ewsLog.getMethodName();
            duration = System.currentTimeMillis() - ewsLog.getExecutionStartTime();
        }

        if (e instanceof EWSException) {

            ewsErrorCode = e.getEwsErrorCode() == null?"NA":e.getEwsErrorCode();
            ewsErrorMessage = e.getEwsErrorMessage() == null?"NA":e.getEwsErrorMessage();
            downstreamErrorCode = e.getDownstreamErrorCode() == null?"NA":e.getDownstreamErrorCode();
            downstreamErrorMessage = e.getDownstreamErrorMessage() == null? "NA": e.getDownstreamErrorMessage();

        }
        if (request != null) {
            userName = request.getHeader("username");
            applicationName = request.getHeader("applicationName");
            migrationIndicator = request.getHeader("migrationIndicator");

        }
        ErrorResponse er = new ErrorResponse(e.getLocalizedMessage(), ewsErrorCode, ewsErrorMessage, downstreamErrorCode, downstreamErrorMessage);
        logException(httpStatus, er, e);
        LOGGER.error("ERROR Executing ::: [[" + sourceType + "]] - [[" + httpMethod + "]] - [[" + sourceName + "]] - [[" + methodName + "]] " +
                "- [[" + CorrelationId.getId() + "]] - [[" + userName + "]] - [[" + applicationName + "]]  " + "- [[" + migrationIndicator + "]] " +
                ":::>>> STOPWATCH :::>>> [[" + duration + "ms]] - [[" + e.getEwsErrorCode() + "]] - " +
                "[[" + e.getEwsErrorMessage() + "]] - [[" + e.getDownstreamErrorCode() + "]] - [[" + e.getDownstreamErrorMessage() + "]]");

        return new ResponseEntity<ErrorResponse>(er, httpStatus);
    }

}