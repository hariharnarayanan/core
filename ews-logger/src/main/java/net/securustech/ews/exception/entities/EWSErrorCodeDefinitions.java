package net.securustech.ews.exception.entities;


public enum EWSErrorCodeDefinitions {

    EWS_BAD_REQUEST_EXCEPTION("400","The request could not be understood by the server due to malformed syntax."),
    EWS_FORBIDDEN_EXCEPTION("403","The server understood the request, but is refusing to fulfill it."),
    EWS_METHOD_NOT_ALLOWED_EXCEPTION("405","The method specified in the Request-Line is not allowed for the resource identified by the Request-URI."),
    EWS_NOT_FOUND_EXCEPTION("404","The server has not found anything matching the Request-URI."),
    EWS_PAYLOAD_TOO_LARGE_EXCEPTION("413","The server is refusing to process a request because the request payload is larger than the server is willing or able to process."),
    EWS_REQUEST_TIMEOUT_EXCEPTION("408","The client did not produce a request within the time that the server was prepared to wait."),
    EWS_UNAUTHORIZED_EXCEPTION("401","The request requires user authentication. The response MUST include a authentication header field."),
    EWS_SQL_EXCEPTION("500","Database operation failed."),
    EWS_DATE_PARSE_ERROR("501", "Invalid Date/Format, it cannot be parsed correctly"),
    EWS_DATE_FORMAT_ERROR("998", "Error While Parsing Date"),
    EWS_RUN_TIME_ERROR("999", "Runtime exception");

    private String ewsErrorMessage;
    private String ewsErrorCode;

    EWSErrorCodeDefinitions(String ewsCode, String ewsMessage){
        this.ewsErrorCode = ewsCode;
        this.ewsErrorMessage = ewsMessage;
    }

    public String getEwsErrorMessage() {
        return ewsErrorMessage;
    }

    public String getEwsErrorCode() {
        return ewsErrorCode;
    }

}
