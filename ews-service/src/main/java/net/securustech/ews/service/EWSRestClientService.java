package net.securustech.ews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.ews.core.repository.entity.ExternalSystemRetryLog;
import net.securustech.ews.core.repository.entity.RetryStatus;
import net.securustech.ews.exception.entities.EWSException;
import net.securustech.ews.exception.entities.EWSRetryableException;
import net.securustech.ews.service.types.EWSRest;
import net.securustech.ews.service.types.ExternalOperation;
import net.securustech.ews.service.types.HTTPRequestMethod;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Map;

import static net.securustech.ews.service.CoreErrorMessages.EXTERNAL_SYSTEM_REQUEST_FAILED;

@Component
public class EWSRestClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EWSRestClientService.class);

    @Autowired
    private ExternalSystemRetryLogService externalSystemRetryLogService;

    @Autowired
    private ExternalRestClient externalRestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.application.name:EWS_APP_USER}")
    private String applicationName;

    @Value("${ews.retry.count.max:0}")
    private Short ewsRetryCountMax;

    public Response durableSend(String requestURL, HTTPRequestMethod httpMethod, String request, ExternalOperation externalOperation, String mediaType) throws Exception {
        return durableSend(requestURL, httpMethod, request, null, externalOperation, mediaType);
    }

    public Response send(String requestURL, HTTPRequestMethod httpMethod, String request, ExternalOperation externalOperation, String mediaType) throws Exception {
        return send(requestURL, httpMethod, request, null, externalOperation, mediaType);
    }

    public Response durableSend(String requestURL, HTTPRequestMethod httpMethod, String request, ExternalOperation externalOperation) throws Exception {
        return durableSend(requestURL, httpMethod, request, null, externalOperation, MediaType.APPLICATION_JSON);
    }

    public Response send(String requestURL, HTTPRequestMethod httpMethod, String request, ExternalOperation externalOperation) throws Exception {
        return send(requestURL, httpMethod, request, null, externalOperation, MediaType.APPLICATION_JSON);
    }

    public Response durableSend(String requestURL, HTTPRequestMethod httpMethod, String request, MultivaluedMap<String, Object> headerMap, ExternalOperation externalOperation) throws Exception {
        return durableSend(requestURL, httpMethod, request, headerMap, externalOperation, MediaType.APPLICATION_JSON);
    }

    public Response send(String requestURL, HTTPRequestMethod httpMethod, String request, MultivaluedMap<String, Object> headerMap, ExternalOperation externalOperation) throws Exception {
        return send(requestURL, httpMethod, request, headerMap, externalOperation, MediaType.APPLICATION_JSON);
    }

    //NOTE -- ONLY MEANT TO BE USED BY RETRY COMPONENT
    public Response durableSend(ExternalSystemRetryLog externalSystemRetryLog, ExternalOperation externalOperation) throws Exception {

        MultivaluedMap<String, Object> headers = null;
        if(externalSystemRetryLog.getHttpHeaders() != null && !("null").equalsIgnoreCase(externalSystemRetryLog.getHttpHeaders()))
            headers =  new MultivaluedHashMap<>(objectMapper.readValue(externalSystemRetryLog.getHttpHeaders(), Map.class));

        LOGGER.info("SEND@REST@REQUEST@START on EndPoint [[[ " + externalSystemRetryLog.getEndpointUrl() + " ]]] :::-> HTTP-Method [[[ " + externalSystemRetryLog.getHttpMethods() + " ]]] :::-> ExternalOperation [[[ " + externalOperation.getOperationType() + " ]]] :::-> MESSAGE ::: \n<<<\n" + externalSystemRetryLog.getPayload() + "\n>>>\n");

        Response response = null;

        try {

            response = sendWithoutRetry(externalSystemRetryLog.getEndpointUrl(), HTTPRequestMethod.valueOf(externalSystemRetryLog.getHttpMethods()), externalSystemRetryLog.getPayload(), headers, externalOperation, MediaType.APPLICATION_JSON);

            externalSystemRetryLog.setStatus(RetryStatus.SUCCESS.getStatus());

            LOGGER.info("SEND@REST@REQUEST@DONE :::-> " + response);

        } catch (Exception e) {

            if (externalSystemRetryLog.getRetryCount() < ewsRetryCountMax) {

                externalSystemRetryLog.setStatus(RetryStatus.IN_PROGRESS.getStatus());
            } else {

                externalSystemRetryLog.setStatus(RetryStatus.FAILED.getStatus());
            }

            LOGGER.error("SEND@REST@REQUEST@FAILED :::-> " + ExceptionUtils.getStackTrace(e));

        } finally {

            externalSystemRetryLog.incrementRetryCount();
            externalSystemRetryLog.setModifiedBy(applicationName);
            externalSystemRetryLog.setModifiedDate(new Date());
            logFailedRestAttemptInDB(externalSystemRetryLog);
        }
        return response;
    }

    public Response durableSend(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, ExternalOperation externalOperation, String mediaType) throws Exception {

        LOGGER.info("SEND@REST@REQUEST@START on EndPoint [[[ " + endpointURL + " ]]] :::-> HTTP-Method [[[ " + httpMethod + " ]]] :::-> ExternalOperation [[[ " + externalOperation.getOperationType() + " ]]] :::-> MESSAGE ::: \n<<<\n" + messagePayload + "\n>>>\n");

        Response response = null;

        try {

            response = sendWithRetry(endpointURL, httpMethod, messagePayload, headers, externalOperation, mediaType);

        } catch (Exception e) {

            LOGGER.error("SEND@REST@REQUEST@FAILED :::-> " + ExceptionUtils.getStackTrace(e));

            try {

                logFailedRestAttemptInDB(new EWSRest(httpMethod, objectMapper.writeValueAsString(headers), endpointURL), messagePayload, e);

            } catch (Exception e1) {
                LOGGER.error("SEND@REST@REQUEST@WRITE-TO-DB@FAILED :::-> " + ExceptionUtils.getStackTrace(e1), e1);
            }
        }

        return response;
    }

    public Response send(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, ExternalOperation externalOperation, String mediaType) throws Exception {

        LOGGER.info("SEND@REST@REQUEST@START on EndPoint [[[ " + endpointURL + " ]]] :::-> HTTP-Method [[[ " + httpMethod + " ]]] :::-> ExternalOperation [[[ " + externalOperation.getOperationType() + " ]]] :::-> MESSAGE ::: \n<<<\n" + messagePayload + "\n>>>\n");

        Response response = null;

        try {

            response = sendWithRetry(endpointURL, httpMethod, messagePayload, headers, externalOperation, mediaType);

        } catch (Exception e) {

            LOGGER.error("SEND@REST@REQUEST@FAILED :::-> " + ExceptionUtils.getStackTrace(e));
        }

        return response;
    }

    private void logFailedRestAttemptInDB(final EWSRest ewsRest, final String messagePayload, Exception e) throws EWSException {

        try {

            if (HTTPRequestMethod.GET != ewsRest.getHttpMethod()) {

                externalSystemRetryLogService.save(ewsRest, messagePayload);
            }
        } catch (EWSException e1) {

            LOGGER.error("EXTERNAL@RETRY@REST@LOG@FAILED :::-> " + ExceptionUtils.getStackTrace(e1));
            e1.addSuppressed(e);
            throw e1;
        }
    }

    private void logFailedRestAttemptInDB(final ExternalSystemRetryLog externalSystemRetryLog) throws EWSException {

        try {

            if (HTTPRequestMethod.GET.getValue() != externalSystemRetryLog.getHttpMethods()) {

                externalSystemRetryLogService.save(externalSystemRetryLog);
            }

        } catch (EWSException e1) {

            LOGGER.error("EXTERNAL@RETRY@REST@LOG@FAILED :::-> " + ExceptionUtils.getStackTrace(e1));
            throw e1;
        }
    }

    @Retryable(value = EWSRetryableException.class, maxAttempts = 4, backoff = @Backoff(delay = 5000L, maxDelay = 30000L, multiplier = 2.0D))
    public Response sendWithRetry(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, ExternalOperation externalOperation, String mediaType) throws Exception {

        LOGGER.info("SEND@REST@REQUEST@START on EndPoint [[[ " + endpointURL + " ]]] :::-> HTTP-Method [[[ " + httpMethod + " ]]] :::-> ExternalOperation [[[ " + externalOperation.getOperationType() + " ]]] :::-> MESSAGE ::: \n<<<\n" + messagePayload + "\n>>>\n");

        try {

            return routeToExternalSystem(endpointURL, httpMethod, messagePayload, headers, externalOperation, mediaType);

        } catch (Exception e) {

            LOGGER.error("ExternalRestClient@REQUEST@FAILED [[[" + externalOperation.getExternalSystem() + "]]] :::>>> " + e.getMessage());
            LOGGER.info("\nFAILED@sendRestRequest >>> Retrying Now >>>\n");
            throw new EWSRetryableException(e.getMessage(), EXTERNAL_SYSTEM_REQUEST_FAILED.getErrorCode(), EXTERNAL_SYSTEM_REQUEST_FAILED.getErrorMessage());
        }
    }

    public Response sendWithoutRetry(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, ExternalOperation externalOperation, String mediaType) throws Exception {

        LOGGER.info("SEND@REST@REQUEST@START on EndPoint [[[ " + endpointURL + " ]]] :::-> HTTP-Method [[[ " + httpMethod + " ]]] :::-> ExternalOperation [[[ " + externalOperation.getOperationType() + " ]]] :::-> MESSAGE ::: \n<<<\n" + messagePayload + "\n>>>\n");

        try {

            return routeToExternalSystem(endpointURL, httpMethod, messagePayload, headers, externalOperation, mediaType);

        } catch (Exception e) {
            LOGGER.error("ExternalRestClient@REQUEST@FAILED [[[" + externalOperation.getExternalSystem() + "]]] :::>>> " + e.getMessage());
            throw new EWSException(e, EXTERNAL_SYSTEM_REQUEST_FAILED.getErrorCode(), EXTERNAL_SYSTEM_REQUEST_FAILED.getErrorMessage());
        }
    }

    public Response routeToExternalSystem(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, ExternalOperation externalOperation, String mediaType) throws Exception {

        switch (externalOperation.getExternalSystem()) {

            case BO:
                return externalRestClient.callRestForBO(endpointURL, httpMethod, messagePayload, headers, mediaType);
            case SVV:
                return externalRestClient.callRestForSVV(endpointURL, httpMethod, messagePayload, headers, mediaType);
            case SCHEDULER:
                return externalRestClient.callRestForSCHEDULER(endpointURL, httpMethod, messagePayload, headers, mediaType);
            case EMS:
                return externalRestClient.callRestForEMS(endpointURL, httpMethod, messagePayload, headers, mediaType);
            case EWS:
                return externalRestClient.callRestForEWS(endpointURL, httpMethod, messagePayload, headers, mediaType);
            default:
                return externalRestClient.callRest(endpointURL, httpMethod, messagePayload, headers, mediaType);
        }
    }
}
