package net.securustech.ews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.ews.core.repository.ExternalSystemRetryLogRepository;
import net.securustech.ews.core.repository.entity.ExternalSystemRetryLog;
import net.securustech.ews.core.repository.entity.RetryStatus;
import net.securustech.ews.exception.entities.ESPNonRetryableException;
import net.securustech.ews.exception.entities.ESPRetryableException;
import net.securustech.ews.exception.entities.EWSException;
import net.securustech.ews.logger.EWSLogger;
import net.securustech.ews.logger.LogLevel;
import net.securustech.ews.logger.SourceType;
import net.securustech.ews.service.types.EMBSTopic;
import net.securustech.ews.service.types.EWSRest;
import net.securustech.ews.service.types.EWSRetryId;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static net.securustech.ews.service.CoreErrorMessages.EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND;
import static net.securustech.ews.service.CoreErrorMessages.EXTERNAL_SYSTEM_RETRY_LOG_SAVE_FAILED;

@Component
public class ExternalSystemRetryLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalSystemRetryLogService.class);

    @Autowired
    private ExternalSystemRetryLogRepository externalSystemRetryLogRepository;

    @Autowired
    private HttpServletRequest request;

    @Value("${spring.application.name:anonymous}")
    private String applicationName;

    @Value("${ews.retry.count.max:0}")
    private Short ewsRetryCountMax;

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true, logLevel = LogLevel.DEBUG)
    public ExternalSystemRetryLog save(ConsumerRecord<String, String> consumerRecord) throws EWSException {

        ExternalSystemRetryLog savedExternalRetryLog;

        try {

            savedExternalRetryLog = this.save(
                    new EMBSTopic(consumerRecord.topic(), consumerRecord.key()),
                    consumerRecord.value());

        } catch (Exception e) {

            throw (EWSException) e;
        }
        return savedExternalRetryLog;
    }

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true, logLevel = LogLevel.DEBUG)
    public ExternalSystemRetryLog save(ExternalSystemRetryLog externalSystemRetryLog) throws EWSException {

        ExternalSystemRetryLog savedExternalRetryLog;

        LOGGER.info("START@PERSIST@RETRY@LOG :::> TOPIC :::> " + externalSystemRetryLog.getTopic() + " :::> PAYLOAD :::> " + externalSystemRetryLog.getPayload());

        try {

            savedExternalRetryLog = externalSystemRetryLogRepository.save(externalSystemRetryLog);

            LOGGER.info("DONE@PERSIST@RETRY@LOG :::> RETRY_ID :::> " + savedExternalRetryLog.getRetryId() + " :::> TOPIC :::> " + savedExternalRetryLog.getTopic() + " :::> PAYLOAD :::> " + savedExternalRetryLog.getPayload());

        } catch (Exception e) {

            throw new EWSException(e, EXTERNAL_SYSTEM_RETRY_LOG_SAVE_FAILED.getErrorCode(),
                    EXTERNAL_SYSTEM_RETRY_LOG_SAVE_FAILED.getErrorMessage() +
                            " : ExternalSystemRetryLog :-> " + externalSystemRetryLog);
        }

        return savedExternalRetryLog;
    }

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true,  logLevel = LogLevel.DEBUG)
    public void saveExternalSystemFailedEvent(ConsumerRecord<String, String> consumerRecord, RetryStatus retryStatus)  {

        ExternalSystemRetryLog savedExternalRetryLog;

        //LOGGER.info("START@PERSIST@RETRY@LOG :::> TOPIC :::> " + externalSystemRetryLog.getTopic() + " :::> PAYLOAD :::> " + externalSystemRetryLog.getPayload());


        Headers headers = consumerRecord.headers();
        String retryId = null;

        if (null != headers) {
            retryId = headers.lastHeader("retryId") != null ? (String) deserialize(headers.lastHeader("retryId").value()) : null;
        }
        try {

            if (null != headers && null != retryId && !retryId.isEmpty()) {
                ExternalSystemRetryLog externalSystemRetryLog = getByRetryId(Long.valueOf(retryId));
                if (retryStatus == RetryStatus.SUCCESS || retryStatus == RetryStatus.FAILED && externalSystemRetryLog.getRetryCount() >= ewsRetryCountMax) {
                    externalSystemRetryLog.setStatus(retryStatus.getStatus());
                } else if (retryStatus == RetryStatus.FAILED && externalSystemRetryLog.getStatus().equalsIgnoreCase(RetryStatus.NEW.getStatus())) {
                    externalSystemRetryLog.setStatus(RetryStatus.IN_PROGRESS.getStatus());
                }
                externalSystemRetryLog.incrementRetryCount();
                externalSystemRetryLog.setModifiedBy(applicationName);
                externalSystemRetryLog.setModifiedDate(new Date());
                save(externalSystemRetryLog);
                // }
            } else if (retryStatus == RetryStatus.FAILED) {
                save(
                        new ExternalSystemRetryLog(
                                RetryStatus.NEW.getStatus(),
                                Short.valueOf("0"),
                                consumerRecord.topic(),
                                consumerRecord.key(),
                                null,
                                null,
                                consumerRecord.value(),
                                null,
                                null,
                                applicationName,
                                new Date(),
                                null,
                                null));
            }
        } catch (Exception e) {
            LOGGER.error("saveExternalSystemFailedEvent failed , Status :::> " + retryStatus.getStatus());

        }


    }

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true,  logLevel = LogLevel.DEBUG)
    public ExternalSystemRetryLog save(EMBSTopic topic, String messagePayload) throws EWSException {

        ExternalSystemRetryLog savedExternalRetryLog = null;

        LOGGER.info("START@PERSIST@RETRY@LOG :::> TOPIC :::> " + topic.getTopic() + " :::> PAYLOAD :::> " + messagePayload);

        try {

            savedExternalRetryLog = new ExternalSystemRetryLog(
                    RetryStatus.NEW.getStatus(),
                    Short.valueOf("0"),
                    topic.getTopic(),
                    topic.getKey(),
                    null,
                    null,
                    messagePayload,
                    getRequestHeaders(),
                    null,
                    applicationName,
                    new Date(),
                    null,
                    null
            );

            savedExternalRetryLog = externalSystemRetryLogRepository.save(savedExternalRetryLog);

            LOGGER.info("DONE@PERSIST@RETRY@LOG :::> RETRY_ID :::> " + savedExternalRetryLog.getRetryId() + " :::> TOPIC :::> " + savedExternalRetryLog.getTopic() + " :::> PAYLOAD :::> " + savedExternalRetryLog.getPayload());

        } catch (Exception e) {

            ExceptionUtils.printRootCauseStackTrace(e);
            throw new EWSException(e, EXTERNAL_SYSTEM_RETRY_LOG_SAVE_FAILED.getErrorCode(),
                    EXTERNAL_SYSTEM_RETRY_LOG_SAVE_FAILED.getErrorMessage() +
                            " : ExternalSystemRetryLog :-> " + savedExternalRetryLog);
        }

        return savedExternalRetryLog;
    }

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true, logLevel = LogLevel.DEBUG)
    public ExternalSystemRetryLog save(EWSRest ewsRest, String messagePayload) throws EWSException {

        ExternalSystemRetryLog savedExternalRetryLog = null;

        LOGGER.info("START@PERSIST@RETRY@LOG :::> HTTP-METHOD :::> " + ewsRest.getHttpMethod() + " :::> HTTP-HEADERS :::> " + ewsRest.getHttpHeaders() + " :::> ENDPOINT :::> " + ewsRest.getEndpointUrl() + " :::> PAYLOAD :::> " + messagePayload);

        try {

            savedExternalRetryLog = new ExternalSystemRetryLog(
                    RetryStatus.NEW.getStatus(),
                    Short.valueOf("0"),
                    null,
                    null,
                    null,
                    ewsRest.getEndpointUrl(),
                    messagePayload,
                    ewsRest.getHttpHeaders(),
                    ewsRest.getHttpMethod().getValue(),
                    applicationName,
                    new Date(),
                    null,
                    null
            );

            savedExternalRetryLog = externalSystemRetryLogRepository.save(savedExternalRetryLog);

            LOGGER.info("DONE@PERSIST@RETRY@LOG :::> RETRY_ID :::> " + savedExternalRetryLog.getRetryId() + " :::> HTTP-METHOD :::> " + ewsRest.getHttpMethod() + " :::> HTTP-HEADERS :::> " + ewsRest.getHttpHeaders() + " :::> ENDPOINT :::> " + ewsRest.getEndpointUrl() + " :::> PAYLOAD :::> " + messagePayload);

        } catch (Exception e) {

            ExceptionUtils.printRootCauseStackTrace(e);
            throw new EWSException(e, EXTERNAL_SYSTEM_RETRY_LOG_SAVE_FAILED.getErrorCode(),
                    EXTERNAL_SYSTEM_RETRY_LOG_SAVE_FAILED.getErrorMessage() +
                            " : ExternalSystemRetryLog :-> " + savedExternalRetryLog);
        }

        return savedExternalRetryLog;
    }

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true,  logLevel = LogLevel.DEBUG)
    public ExternalSystemRetryLog getByRetryId(Long retryId) throws EWSException {

        try {

            return externalSystemRetryLogRepository.findById(retryId).get();
        } catch (Exception e) {

            throw new EWSException(e, EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND.getErrorCode(),
                    EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND.getErrorMessage());
        }
    }

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true, logLevel = LogLevel.DEBUG)
    public Iterable<ExternalSystemRetryLog> getByStatus(List<RetryStatus> retryStatusList) throws EWSException {

        try {

            return externalSystemRetryLogRepository.findByStatus(
                    retryStatusList.stream().map(
                            retryStatus -> retryStatus.getStatus()).collect(Collectors.toList())
            );
        } catch (Exception e) {

            throw new EWSException(e, EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND.getErrorCode(),
                    EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND.getErrorMessage());
        }
    }

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true, logLevel = LogLevel.DEBUG)
    public Iterable<ExternalSystemRetryLog> getByStatusAndRetryCount(List<RetryStatus> retryStatusList, Short retryCount) throws EWSException {

        try {

            return externalSystemRetryLogRepository.findByStatusAndRetryCount(
                    retryStatusList.stream().map(
                            retryStatus -> retryStatus.getStatus()).collect(Collectors.toList())
                    , retryCount);
        } catch (Exception e) {

            throw new EWSException(e, EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND.getErrorCode(),
                    EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND.getErrorMessage());
        }
    }

    @EWSLogger(type = SourceType.DATABASE_ORACLE, logResults = true,  logLevel = LogLevel.DEBUG)
    public Iterable<ExternalSystemRetryLog> getByStatusAndRetryCountAndUser(List<RetryStatus> retryStatusList, Short retryCount, String createBy) throws EWSException {

        try {

            return externalSystemRetryLogRepository.findByStatusAndRetryCountAndUser(
                    retryStatusList.stream().map(
                            retryStatus -> retryStatus.getStatus()).collect(Collectors.toList())
                    , retryCount, createBy);
        } catch (Exception e) {

            throw new EWSException(e, EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND.getErrorCode(),
                    EXTERNAL_SYSTEM_RETRY_LOG_DATA_NOT_FOUND.getErrorMessage());
        }
    }

    private String getRequestHeaders() throws Exception {

        HashMap<String, Object> requestHeaders = new HashMap();
        Enumeration enumeration = this.request.getHeaderNames();

        while (enumeration.hasMoreElements()) {

            String headerName = (String) enumeration.nextElement();
            String headerValue = this.request.getHeader(headerName);
            LOGGER.info("HttpRequest@Header :-> " + headerName + " : " + headerValue);
            if (!headerName.equalsIgnoreCase("connection") && !headerName.equalsIgnoreCase("origin") && !headerName.equalsIgnoreCase("user-agent") && !headerName.equalsIgnoreCase("cache-control") && !headerName.equalsIgnoreCase("content-type") && !headerName.equalsIgnoreCase("postman-token") && !headerName.equalsIgnoreCase("accept") && !headerName.equalsIgnoreCase("content-length") && !headerName.equalsIgnoreCase("accept-encoding") && !headerName.equalsIgnoreCase("accept-language")) {
                requestHeaders.put(headerName, headerValue);
            }
        }

        requestHeaders.put("accessTime", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ")).format(new Date()));
        Iterator var5 = requestHeaders.keySet().iterator();

        while (var5.hasNext()) {
            Object header = var5.next();
            LOGGER.info("HttpRequestMAP@Header :-> " + header + " : " + requestHeaders.get(header));
        }
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(requestHeaders);

    }

    private Object deserialize(byte[] data) {

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = null;
        Object obj = null;
        try {
            is = new ObjectInputStream(in);
            obj = is.readObject();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return obj;
    }

    public void resolveExceptionLevel(Exception originalException, String ewsErrorCode, String ewsErrorMessage) throws  Exception{
        if(EWSRetryId.getId() == null) {
            throw new ESPRetryableException(originalException.getMessage(), ewsErrorCode, ewsErrorMessage);
        } else {
            throw new ESPNonRetryableException(originalException.getMessage(), ewsErrorCode, ewsErrorMessage);
        }
    }
}