package net.securustech.ews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.ews.exception.entities.EWSException;
import net.securustech.ews.exception.entities.EWSRetryableException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static net.securustech.ews.service.types.ExternalOperation.EMS_SEND_NOTIFICATION;
import static net.securustech.ews.service.types.ExternalOperation.EWS_CUSTOMERS_GET_RELATIONSHIPS;
import static net.securustech.ews.service.types.HTTPRequestMethod.POST;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {EMBSPublisherServiceTestConfig.class, ObjectMapper.class})
public class EWSRestClientServiceExceptionTest {

    @Autowired
    private EWSRestClientService ewsRestClientService;

    private static String EWS_ENDPOINT = "www.securustech.net/ews/api";

    @BeforeClass
    public static void initializeEMBS() throws Exception {

        System.setProperty("spring.cloud.zookeeper.connect-string", "10.6.247.197:2181");
        System.setProperty("spring.kafka.enabled", "true");
        System.setProperty("spring.application.name", "ews-core");
    }

    @Test(expected = EWSException.class)
    public void whenRestClientThrowsException() throws Exception {

        ewsRestClientService.sendWithoutRetry(EWS_ENDPOINT, null, "Shift Left!!!", null, EWS_CUSTOMERS_GET_RELATIONSHIPS, APPLICATION_JSON);
    }

    @Test(expected = EWSRetryableException.class)
    public void whenRestClientThrowsExceptionDuringRetry() throws Exception {

        ewsRestClientService.sendWithRetry(null, POST, "Sustainable Pace!!!", null, EMS_SEND_NOTIFICATION, APPLICATION_XML);
    }
}