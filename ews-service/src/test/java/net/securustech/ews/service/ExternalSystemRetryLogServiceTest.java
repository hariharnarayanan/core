package net.securustech.ews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import net.securustech.ews.core.repository.ExternalSystemRetryLogRepository;
import net.securustech.ews.core.repository.entity.ExternalSystemRetryLog;
import net.securustech.ews.core.repository.entity.RetryStatus;
import net.securustech.ews.service.types.EWSRest;
import net.securustech.ews.service.types.HTTPRequestMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {EMBSPublisherServiceTestConfig.class, ObjectMapper.class})
public class ExternalSystemRetryLogServiceTest {

    @Autowired
    private ExternalSystemRetryLogRepository externalSystemRetryLogRepository;

    @Autowired
    private ExternalSystemRetryLogService externalSystemRetryLogService;

    private static String TOPIC = "EWS_CORE_UPSERT";
    private static String KEY = "EWS_CORE_UPSERT";
    private static String TOPIC_EVENT = "CREATE_VISIT";
    private static String MESSAGE_PAYLOAD = "Kudos to MW Ecosystem!!!";
    private static String CREATED_BY_NG = "NG";
    private static String CREATED_BY_SO = "SO";
    private static String CREATED_BY_SCHEDULER = "SCHEDULER";
    private static String ENDPOINT_URL = "http://mw.wonderland.org/heavenly";
    private static HTTPRequestMethod HTTP_METHOD = HTTPRequestMethod.POST;
    private static Map<String,String> HTTP_HEADERS = ImmutableMap.of(
            "EWS", "Enterprice Web Services",
            "ESP", "Enterprice Streaming Platform",
            "EMBS", "Enterprice Message Broker Services",
            "GHOST", "Enterprice Streaming Process"
            );

    @BeforeClass
    public static void initializeEMBS() throws Exception {

        System.setProperty("spring.cloud.zookeeper.connect-string", "10.6.247.197:2181");
        System.setProperty("spring.kafka.enabled", "true");
        System.setProperty("spring.application.name", "ews-core");
    }

    @Before
    public void setUp() throws Exception {

        externalSystemRetryLogService.save(
                new ExternalSystemRetryLog(
                        RetryStatus.IN_PROGRESS.getStatus(),
                        Short.valueOf("2"),
                        TOPIC,
                        KEY,
                        null,
                        null,
                        MESSAGE_PAYLOAD,
                        null,
                        null,
                        CREATED_BY_NG,
                        new Date(),
                        null,
                        null
                ));

        externalSystemRetryLogService.save(
                new ExternalSystemRetryLog(
                        RetryStatus.NEW.getStatus(),
                        Short.valueOf("0"),
                        TOPIC,
                        KEY,
                        null,
                        null,
                        MESSAGE_PAYLOAD,
                        null,
                        null,
                        CREATED_BY_NG,
                        new Date(),
                        null,
                        null
                ));

        externalSystemRetryLogService.save(
                new ExternalSystemRetryLog(
                        RetryStatus.NEW.getStatus(),
                        Short.valueOf("0"),
                        null,
                        null,
                        TOPIC_EVENT,
                        null,
                        MESSAGE_PAYLOAD,
                        null,
                        null,
                        CREATED_BY_SCHEDULER,
                        new Date(),
                        null,
                        null
                ));

        EWSRest ewsRest = new EWSRest(HTTP_METHOD, HTTP_HEADERS, ENDPOINT_URL);

        externalSystemRetryLogService.save(
                new ExternalSystemRetryLog(
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
                ));
    }

    @After
    public void tearDown() throws Exception {

        externalSystemRetryLogRepository.deleteAll();
    }

    @Test
    public void shouldRetrieveAllThatMatchTheStatus() throws Exception {

        Iterable<ExternalSystemRetryLog> externalSystemRetryLogs = externalSystemRetryLogService.getByStatus(Arrays.asList(RetryStatus.NEW));

        int resultCount = 0;
        for (ExternalSystemRetryLog externalSystemRetryLog : externalSystemRetryLogs) {

            assertResults(externalSystemRetryLog);
            resultCount++;
        }
        assertEquals(3, resultCount);

        externalSystemRetryLogRepository.deleteAll();
    }

    @Test
    public void shouldRetrieveAllThatMatchTheStatusAndRetryCount() throws Exception {

        Iterable<ExternalSystemRetryLog> externalSystemRetryLogs = externalSystemRetryLogService.getByStatusAndRetryCount(Arrays.asList(RetryStatus.IN_PROGRESS), Short.valueOf("2"));

        int resultCount = 0;
        for (ExternalSystemRetryLog externalSystemRetryLog : externalSystemRetryLogs) {

            assertEquals(RetryStatus.IN_PROGRESS.getStatus(), externalSystemRetryLog.getStatus());
            assertEquals(Short.valueOf("2"), externalSystemRetryLog.getRetryCount());
            assertEquals(MESSAGE_PAYLOAD, externalSystemRetryLog.getPayload());

            resultCount++;
        }
        assertEquals(1, resultCount);

        externalSystemRetryLogRepository.deleteAll();
    }

    @Test
    public void shouldRetrieveAllThatMatchTheStatusAndRetryCountAndUser() throws Exception {

        Iterable<ExternalSystemRetryLog> externalSystemRetryLogs = externalSystemRetryLogService.getByStatusAndRetryCountAndUser(Arrays.asList(RetryStatus.NEW), Short.valueOf("0"), CREATED_BY_SO);

        int resultCount = 0;
        for (ExternalSystemRetryLog externalSystemRetryLog : externalSystemRetryLogs) {

            assertResults(externalSystemRetryLog);
            resultCount++;
        }
        assertEquals(1, resultCount);

        externalSystemRetryLogRepository.deleteAll();
    }

    @Test
    public void shouldRetrieveAllThatMatchMultipleRetryStatuses() throws Exception {

        Iterable<ExternalSystemRetryLog> externalSystemRetryLogs = externalSystemRetryLogService.getByStatus(Arrays.asList(RetryStatus.NEW, RetryStatus.IN_PROGRESS));

        int resultCount = 0;
        for (ExternalSystemRetryLog externalSystemRetryLog : externalSystemRetryLogs) {

            assertNotEquals(RetryStatus.SUCCESS.getStatus(), externalSystemRetryLog.getStatus());
            assertNotEquals(RetryStatus.FAILED.getStatus(), externalSystemRetryLog.getStatus());
            assertEquals(MESSAGE_PAYLOAD, externalSystemRetryLog.getPayload());
            resultCount++;
        }
        assertEquals(4, resultCount);

        externalSystemRetryLogRepository.deleteAll();
    }

    private void assertResults(ExternalSystemRetryLog externalSystemRetryLog) {

        assertEquals(RetryStatus.NEW.getStatus(), externalSystemRetryLog.getStatus());
        assertEquals(Short.valueOf("0"), externalSystemRetryLog.getRetryCount());
        assertEquals(MESSAGE_PAYLOAD, externalSystemRetryLog.getPayload());
    }
}