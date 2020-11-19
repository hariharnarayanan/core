package net.securustech.ews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import net.securustech.ews.core.repository.ExternalSystemRetryLogRepository;
import net.securustech.ews.core.repository.entity.ExternalSystemRetryLog;
import net.securustech.ews.core.repository.entity.RetryStatus;
import net.securustech.ews.exception.entities.EWSException;
import net.securustech.ews.service.types.EWSRest;
import net.securustech.ews.service.types.ExternalOperation;
import net.securustech.ews.service.types.HTTPRequestMethod;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static net.securustech.ews.logger.ExternalSystem.*;
import static net.securustech.ews.logger.ExternalSystem.UNKNOWN;
import static net.securustech.ews.service.types.ExternalOperation.*;
import static net.securustech.ews.service.types.HTTPRequestMethod.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {EMBSPublisherServiceTestConfig.class, ObjectMapper.class})
public class EWSRestClientServiceTest {

    @Autowired
    private EWSRestClientService ewsRestClientService;

    @Mock
    private EWSRestClientService mockedEWSRestClientService;

    @MockBean
    private ExternalRestClient externalRestClient;

    @Autowired
    private ExternalSystemRetryLogRepository externalSystemRetryLogRepository;

    private Response response;

    private static String BO_ENDPOINT = "www.securustech.net/bo/api";

    private static String SVV_ENDPOINT = "www.securustech.net/svv/api";

    private static String SCHEDULER_ENDPOINT = "www.securustech.net/scheduler/api";

    private static String EMS_ENDPOINT = "www.securustech.net/ems/api";

    private static String EWS_ENDPOINT = "www.securustech.net/ews/api";

    private static String CRM_UNKNOWN_ENDPOINT = "www.securustech.net/crm/api";
    private static String MESSAGE_PAYLOAD = "Kudos to MW Ecosystem!!!";
    private static String CREATED_BY_SO = "SO";
    private static String ENDPOINT_URL = "http://mw.wonderland.org/heavenly";
    private static HTTPRequestMethod HTTP_METHOD = HTTPRequestMethod.POST;
    private static Map<String,String> HTTP_HEADERS = ImmutableMap.of(
            "EWS", "Enterprice Web Services",
            "ESP", "Enterprice Streaming Platform",
            "EMBS", "Enterprice Message Broker Services",
            "GHOST", "Enterprice Streaming Process"
    );

    ExternalSystemRetryLog externalSystemRetryLog;

    @BeforeClass
    public static void initializeEMBS() throws Exception {

        System.setProperty("spring.cloud.zookeeper.connect-string", "10.6.247.197:2181");
        System.setProperty("spring.kafka.enabled", "true");
        System.setProperty("spring.application.name", "ews-core");
    }

    @Before
    public void setUp() throws Exception {

        Response.ResponseBuilder responseBuilder = Response.ok();
        response = responseBuilder.entity("Creativity is Contagious, pass it on!!!").build();

        when(externalRestClient.callRestForBO(anyString(), any(), anyString(), any(), anyString())).thenReturn(response);
        when(externalRestClient.callRestForSVV(anyString(), any(), anyString(), any(), anyString())).thenReturn(response);
        when(externalRestClient.callRestForSCHEDULER(anyString(), any(), anyString(), any(), anyString())).thenReturn(response);
        when(externalRestClient.callRestForEMS(anyString(), any(), anyString(), any(), anyString())).thenReturn(response);
        when(externalRestClient.callRestForEWS(anyString(), any(), anyString(), any(), anyString())).thenReturn(response);
        when(externalRestClient.callRest(anyString(), any(), anyString(), any(), anyString())).thenReturn(response);

        EWSRest ewsRest = new EWSRest(HTTP_METHOD, HTTP_HEADERS, ENDPOINT_URL);

        externalSystemRetryLog = new ExternalSystemRetryLog(
                RetryStatus.NEW.getStatus(),
                Short.valueOf("0"),
                null,
                null,
                null,
                ewsRest.getEndpointUrl(),
                MESSAGE_PAYLOAD,
                ewsRest.getHttpHeaders(),
                ewsRest.getHttpMethod().getValue(),
                CREATED_BY_SO,
                new Date(),
                null,
                null
        );
    }

    @Test
    public void shouldLogFailedRestEventToDB() throws Exception {

        attemptEWSRest(HTTPRequestMethod.POST);

        Iterable<ExternalSystemRetryLog> externalSystemRetryLogs = externalSystemRetryLogRepository.findAll();
        System.out.println("SAVED@RETRY@LOG :::>>> " + externalSystemRetryLogs);

        externalSystemRetryLogs.forEach(externalSystemRetryLog -> assertResults(externalSystemRetryLog));

        externalSystemRetryLogRepository.deleteAll();
    }

    @Test
    public void shouldNOTLogFailedRestEventToDBForNON_DURABLE_SEND() throws Exception {

        attemptNonDurableEWSRest();

        Iterable<ExternalSystemRetryLog> externalSystemRetryLogs = externalSystemRetryLogRepository.findAll();
        System.out.println("EMPTY@RETRY@LOG :::>>> " + externalSystemRetryLogs);

        externalSystemRetryLogs.forEach(externalSystemRetryLog -> assertResults(externalSystemRetryLog));
        assertFalse(externalSystemRetryLogs.iterator().hasNext());

        externalSystemRetryLogRepository.deleteAll();
    }

    @Test
    public void shouldNOTLogFailedRestEventToDBForDURABLE_SEND_GET_METHOD() throws Exception {

        attemptEWSRest(HTTPRequestMethod.GET);

        Iterable<ExternalSystemRetryLog> externalSystemRetryLogs = externalSystemRetryLogRepository.findAll();
        System.out.println("EMPTY@RETRY@LOG :::>>> " + externalSystemRetryLogs);

        externalSystemRetryLogs.forEach(externalSystemRetryLog -> assertResults(externalSystemRetryLog));
        assertFalse(externalSystemRetryLogs.iterator().hasNext());

        externalSystemRetryLogRepository.deleteAll();
    }

    private void attemptEWSRest(HTTPRequestMethod httpRequestMethod) throws Exception {

        try {

            when(mockedEWSRestClientService.sendWithoutRetry(anyString(), any(), anyString(), any(), any(), anyString())).thenThrow(new EWSException(new RuntimeException("Houston!!! We have a problem!!!")));

            externalSystemRetryLog.setHttpMethods(httpRequestMethod.getValue());
            mockedEWSRestClientService.durableSend(externalSystemRetryLog, ExternalOperation.UNKNOWN);

        } catch (EWSException e) {

            System.out.println("ERROR@REST@EVENT :::>>> " + ExceptionUtils.getStackTrace(e));
        }
    }

    private void attemptNonDurableEWSRest() throws Exception {

        try {

            when(mockedEWSRestClientService.sendWithRetry(anyString(), any(), anyString(), any(), any(), anyString())).thenThrow(new EWSException(new RuntimeException("Houston!!! We have a problem!!!")));

            mockedEWSRestClientService.send(ENDPOINT_URL, HTTPRequestMethod.POST, MESSAGE_PAYLOAD, null, ExternalOperation.UNKNOWN, MediaType.APPLICATION_JSON);

        } catch (EWSException e) {

            System.out.println("ERROR@REST@EVENT :::>>> " + ExceptionUtils.getStackTrace(e));
        }
    }

    private void assertResults(ExternalSystemRetryLog externalSystemRetryLog) {

        assertEquals(RetryStatus.FAILED.getStatus(), externalSystemRetryLog.getStatus());
        assertEquals(Short.valueOf("1"), externalSystemRetryLog.getRetryCount());
        assertEquals(MESSAGE_PAYLOAD, externalSystemRetryLog.getPayload());
        assertNotNull(externalSystemRetryLog.getHttpHeaders());
        System.out.println("SAVED@RETRY@LOG@HTTP_HEADERS :::>>> " + externalSystemRetryLog.getHttpHeaders());
    }

    @Test
    public void shouldMatchBOForBO_CREATE_CUSTOMER() {

        assertEquals(BO, BO_CREATE_CUSTOMER.getExternalSystem());
    }

    @Test
    public void shouldMatchSVVForSVV_ADD_SITE() {

        assertEquals(SVV, SVV_ADD_SITE.getExternalSystem());
    }

    @Test
    public void shouldMatchEMSForEMS_SEND_NOTIFICATION() {

        assertEquals(EMS, EMS_SEND_NOTIFICATION.getExternalSystem());
    }

    @Test
    public void shouldMatchSCHEDULERForSCHEDULER_CANCEL_VISITS() {

        assertEquals(SCHEDULER, SCHEDULER_CANCEL_VISITS.getExternalSystem());
    }

    @Test
    public void shouldMatchEWSForEWS_CUSTOMERS_GET_PRODUCTS() {

        assertEquals(EWS, EWS_CUSTOMERS_GET_PRODUCTS.getExternalSystem());
    }

    @Test
    public void shouldMatchUNKNOWNForUNKNOWN() {

        assertEquals(UNKNOWN, ExternalOperation.UNKNOWN.getExternalSystem());
    }

    @Test
    public void shouldNOTMatchEWSForSVV() {

        assertNotEquals(EWS, SVV_CANCEL_APPOINTMENT.getExternalSystem());
    }

    @Test
    public void shouldCallExternalSystemEndpointWithRetryForBO() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithRetry(BO_ENDPOINT, POST, "Tioga Pass!!!", null, BO_UPDATE_VIDEO_CALL, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithRetryForSVV() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithRetry(SVV_ENDPOINT, GET, "Lake Tahoe!!!", null, SVV_GET_CONFIG, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithRetryForSCHEDULER() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithRetry(SCHEDULER_ENDPOINT, DELETE, "Yosemite Valley!!!", null, SCHEDULER_CANCEL_VISITS, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithRetryForEMS() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithRetry(EMS_ENDPOINT, PUT, "Mystry Spot!!!", null, EMS_SEND_NOTIFICATION, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithRetryForEWS() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithRetry(EWS_ENDPOINT, POST, "Golden Gate!!!", null, EWS_CUSTOMERS_GET_RELATIONSHIPS, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithRetryForDEFAULT() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithRetry(CRM_UNKNOWN_ENDPOINT, POST, "Golden Gate!!!", null, ExternalOperation.UNKNOWN, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithoutRetryForBO() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithoutRetry(BO_ENDPOINT, POST, "Tioga Pass!!!", null, BO_UPDATE_VIDEO_CALL, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithoutRetryForSVV() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithoutRetry(SVV_ENDPOINT, GET, "Lake Tahoe!!!", null, SVV_GET_CONFIG, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithoutRetryForSCHEDULER() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithoutRetry(SCHEDULER_ENDPOINT, DELETE, "Yosemite Valley!!!", null, SCHEDULER_CANCEL_VISITS, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithoutRetryForEMS() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithoutRetry(EMS_ENDPOINT, PUT, "Mystry Spot!!!", null, EMS_SEND_NOTIFICATION, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithoutRetryForEWS() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithoutRetry(EWS_ENDPOINT, POST, "Golden Gate!!!", null, EWS_CUSTOMERS_GET_RELATIONSHIPS, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointWithoutRetryForDEFAULT() throws Exception {

        assertEquals(response, ewsRestClientService.sendWithoutRetry(CRM_UNKNOWN_ENDPOINT, POST, "Golden Gate!!!", null, ExternalOperation.UNKNOWN, APPLICATION_JSON));
    }
}