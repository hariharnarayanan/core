package net.securustech.ews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static net.securustech.ews.service.types.HTTPRequestMethod.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {EMBSPublisherServiceTestConfig.class, ObjectMapper.class})
public class ExternalRestClientTest {

    @Spy
    private ExternalRestClient externalRestClient;

    @Mock
    private Invocation.Builder builder;

    @Mock
    private Client client;

    @Mock
    private WebTarget webTarget;

    private Response response;

    private static String BO_ENDPOINT = "www.securustech.net/bo/api";

    private static String SVV_ENDPOINT = "www.securustech.net/svv/api";

    private static String SCHEDULER_ENDPOINT = "www.securustech.net/scheduler/api";

    private static String EMS_ENDPOINT = "www.securustech.net/ems/api";

    private static String EWS_ENDPOINT = "www.securustech.net/ews/api";

    private static String CRM_UNKNOWN_ENDPOINT = "www.securustech.net/crm/api";

    @BeforeClass
    public static void initializeEMBS() throws Exception {

        System.setProperty("spring.cloud.zookeeper.connect-string", "10.6.247.197:2181");
        System.setProperty("spring.kafka.enabled", "true");
        System.setProperty("spring.application.name", "ews-core");
    }

    @Before
    public void setup() {

        Response.ResponseBuilder responseBuilder = Response.ok();
        response = responseBuilder.entity("Creativity is Contagious, pass it on!!!").build();

        when(externalRestClient.getRestClient()).thenReturn(client);
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);

        when(builder.get()).thenReturn(response);
        when(builder.post(any())).thenReturn(response);
        when(builder.put(any())).thenReturn(response);
        when(builder.delete()).thenReturn(response);
    }

    @Test
    public void shouldCallExternalSystemEndpointForBO() throws Exception {

        assertEquals(response, externalRestClient.callRestForBO(BO_ENDPOINT, POST, "Tioga Pass!!!", null, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointForSVV() throws Exception {

        assertEquals(response, externalRestClient.callRestForSVV(SVV_ENDPOINT, GET, "Lake Tahoe!!!", null, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointForSCHEDULER() throws Exception {

        assertEquals(response, externalRestClient.callRestForSCHEDULER(SCHEDULER_ENDPOINT, DELETE, "Yosemite Valley!!!", null, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointForEMS() throws Exception {

        assertEquals(response, externalRestClient.callRestForEMS(EMS_ENDPOINT, PUT, "Mystry Spot!!!", null, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointForEWS() throws Exception {

        assertEquals(response, externalRestClient.callRestForEWS(EWS_ENDPOINT, POST, "Golden Gate!!!", null, APPLICATION_JSON));
    }

    @Test
    public void shouldCallExternalSystemEndpointForDEFAULT() throws Exception {

        assertEquals(response, externalRestClient.callRest(CRM_UNKNOWN_ENDPOINT, POST, "Golden Gate!!!", null, APPLICATION_JSON));
    }
}
