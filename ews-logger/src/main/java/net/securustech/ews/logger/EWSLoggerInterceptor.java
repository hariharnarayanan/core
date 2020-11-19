package net.securustech.ews.logger;

import net.securustech.embs.util.CorrelationId;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Objects;

@Aspect
@Component
@Order(1)
public class EWSLoggerInterceptor {

    public EWSLoggerInterceptor() {

    }

    @Around("@within(ewsLogger) || @annotation(ewsLogger)")
    public Object profile(ProceedingJoinPoint joinPoint, EWSLogger ewsLogger) throws Throwable {

        Logger logger = getLog(joinPoint);


        SourceType sourceType = getSourceType(ewsLogger);
        LogLevel logLevel = getLogLevel(ewsLogger);
        String uuid = CorrelationId.getId();  //UUID and CorrelationID are same

        String methodName = "NA";
        String sourceName = getSourceName(ewsLogger, joinPoint);
        String httpMethod = "NONE";
        String userName = "NA";
        String applicationName = "NA";
        String migrationIndicator = "NA";

        if (sourceType == SourceType.INTERNAL_ENDPOINT) {

            methodName = getMethodName(ewsLogger);
            httpMethod = ewsLogger.httpMethod();
            userName = getHeader("username");
            applicationName = getHeader("applicationName");
            migrationIndicator = getHeader("migrationIndicator");
            logHeaders(logLevel, logger, uuid, sourceName, sourceType);
        }

        boolean logRequest = (ewsLogger != null) && ewsLogger.logRequest();
        if (logRequest) {
            logRequest(logLevel, logger, uuid, sourceName, sourceType, joinPoint);
        }

        createLog(logLevel, logger, "STARTED Executing ::: [[" + sourceType + "]] - [[" + httpMethod + "]] - [[" + sourceName + "]] - " +
                "- [[" + methodName + "]] - [[" + uuid + "]] - [[" + userName + "]] - [[" + applicationName + "]] - [[" + migrationIndicator + "]] " +
                "- [[ARGS: " + getArgValues(joinPoint) + "]]");

        //capture the start time
        long startTime = System.currentTimeMillis();
        long duration = 0L;
        setEWSLogContext(sourceType, httpMethod, sourceName, methodName, startTime);
        //execute the method and get the result
        Object result = null;
        result = joinPoint.proceed();
        //capture the end time
        duration = System.currentTimeMillis() - startTime;
        boolean logResults = (ewsLogger != null) && ewsLogger.logResults();
        if (logResults) {

            logResults(logLevel, logger, sourceName, uuid, sourceType, joinPoint, result);
        }

        createLog(logLevel, logger, "ENDED Executing ::: [[" + sourceType + "]] - [[" + httpMethod + "]] - [[" + sourceName + "]] " +
                "- [[" + methodName + "]] - [[" + uuid + "]] - [[" + userName + "]] - [[" + applicationName + "]] - [[" + migrationIndicator + "]] " +
                "- :::>>> STOPWATCH :::>>> [[" + duration + "ms]]");

        return result;
    }

    private void setEWSLogContext(SourceType sourceType, String httpMethod, String sourceName, String methodName, Long startTime) {
        EWSLog ewsLog = new EWSLog();
        ewsLog.setSourceType(sourceType.name());
        ewsLog.setHttpMethod(httpMethod);
        ewsLog.setSourceName(sourceName);
        ewsLog.setMethodName(methodName);
        ewsLog.setExecutionStartTime(startTime);
        EWSLogContext.setEwsLog(ewsLog);
    }

    private String getMethodName(EWSLogger ewsLogger) {

        String methodName = "NA";

        if (ewsLogger != null) {
            ewsLogger.name();
            methodName = ewsLogger.name();
        }

        return methodName;
    }

    protected String getHeader(String name) {
        String headerValue = "NA";
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        if (request != null) {
            headerValue = request.getHeader(name);
            if (StringUtils.isNotBlank(headerValue)) {
                headerValue = headerValue.trim().toUpperCase();
            }
        }
        return  headerValue;
    }

    private String getSourceName(EWSLogger ewsLogger, ProceedingJoinPoint joinPoint) {

        String requestPath;

        if (ewsLogger == null) {

            requestPath = joinPoint.getTarget().getClass().getName() + "." + getMethodName(joinPoint);

        } else if (ewsLogger.type() == SourceType.INTERNAL_ENDPOINT) {

            requestPath = (((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest()).getServletPath();

        } else if (ewsLogger.name().length() <= 0) {

            requestPath = joinPoint.getTarget().getClass().getName() + "." + getMethodName(joinPoint);

        } else if(!ewsLogger.name().equals("")){

            requestPath = ewsLogger.name();
        }
        else
        {
            requestPath = joinPoint.getTarget().getClass().getName() + "." + getMethodName(joinPoint);

        }

        return requestPath;
    }


    private SourceType getSourceType(EWSLogger ewsLogger) {

        return (ewsLogger != null) ? ewsLogger.type() : SourceType.NONE;
    }

    private LogLevel getLogLevel(EWSLogger ewsLogger) {

        return (ewsLogger != null) ? ewsLogger.logLevel() : LogLevel.NONE;
    }

    /**
     * Log that we are leaving a method.
     *
     * @param sourceName
     * @param sourceType
     * @param jp         JointPoint
     * @param retVal
     * @return value
     */
    private void logResults(LogLevel logLevel, Logger log, String sourceName, String uuid, SourceType sourceType, JoinPoint jp, Object retVal) {

        createLog(logLevel, log, "RETURNED RESULTS ::: [[" + sourceType + "]] - [[" + sourceName + "]] - [[" + uuid + "]] - [[" + getMethodName(jp) + "]] :::>>> \n<<<\n" + ((retVal != null) ? retVal : "") + "\n>>>");
    }

    /**
     * Get the method that this join point surrounds.
     * Used for logging the entry into a method.
     *
     * @param jp joiPoint
     * @return methodName
     */
    private String getMethodName(JoinPoint jp) {
        MethodSignature met = (MethodSignature) jp.getSignature();
        return met.getMethod().getName();
    }

    /**
     * Log that we are leaving a method.
     *
     * @param jp JointPoint
     * @return argument list string
     */
    private String getArgValues(JoinPoint jp) {

        StringBuilder builder = new StringBuilder();
        builder.append("(");
        boolean hasArgs = false;

        for (Object o : jp.getArgs()) {
            if (o != null) {
                hasArgs = true;
                builder.append("<").append(o.getClass().getSimpleName()).append(">");
                builder.append(o.toString());
                try {
                    builder.append(", ");
                } catch (NullPointerException e) {
                    builder.append("null, ");
                }
            }
        }

        String argStr = builder.toString();
        if (hasArgs) {

            argStr = argStr.substring(0, argStr.length() - 2);
        }

        argStr = argStr + ")";

        return argStr;
    }

    private void logHeaders(LogLevel logLevel, Logger logger, String uuid, String sourceName, SourceType sourceType) {

        HttpServletRequest req = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        createLog(logLevel, logger, "REQUEST HEADERS ::: [[" + sourceType + "]] - [[" + sourceName + "]] - [[" + uuid + "]] :::>>>");
        createLog(logLevel, logger, "<<<");

        Enumeration headerNames = req.getHeaderNames();

        while (headerNames.hasMoreElements()) {

            String headerName = headerNames.nextElement().toString();
            String headerValue = req.getHeader(headerName);
            createLog(logLevel, logger, "\t" + headerName + " ||| " + headerValue);
        }

        createLog(logLevel, logger, ">>>");
    }

    private void logRequest(LogLevel logLevel, Logger logger, String uuid, String sourceName, SourceType sourceType, ProceedingJoinPoint thisJoinPoint) {

        createLog(logLevel, logger, "REQUEST BODY ::: [[" + sourceType + "]] - [[" + sourceName + "]] - [[\" + uuid + \"]] - [[\"" + uuid + "\"]] :::>>>");
        createLog(logLevel, logger, "<<<");

        for (final Object argument : thisJoinPoint.getArgs()) {

            if (argument != null && !(argument instanceof String) && !(argument instanceof Long) && !(argument instanceof Integer) && !(argument instanceof Boolean)) {
                createLog(logLevel, logger, "\t" + new JSONObject(argument));
            }
        }

        createLog(logLevel, logger, ">>>");
    }

    private Logger getLog(JoinPoint jp) {
        return LoggerFactory.getLogger(jp.getTarget().getClass());
    }

    private void createLog(LogLevel logLevel, Logger logger, String logStr) {

        switch (logLevel) {
            case TRACE:
                logger.trace(logStr);
                break;

            case DEBUG:
                logger.debug(logStr);
                break;

            case INFO:
                logger.info(logStr);
                break;

            case WARN:
                logger.warn(logStr);
                break;

            case ERROR:
                logger.error(logStr);
                break;

            default:
                logger.info(logStr);
                break;
        }
    }
}