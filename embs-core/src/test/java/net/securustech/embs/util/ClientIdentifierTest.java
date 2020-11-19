package net.securustech.embs.util;

import net.securustech.embs.EmbsAutoConfiguration;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@ContextConfiguration(classes = EmbsAutoConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClientIdentifierTest {

    private static final String CUSTOM_CLIENT_ID = "ng";
    private static final String GROUP_ID = "data_change";
    private static final String LOCAL_SERVER_PORT = "9999";
    private static final String APPLICATION_NAME = "esp-syslog";

    @BeforeClass
    public static void setUp() throws Exception {

        System.setProperty("spring.kafka.bootstrap-servers", "ld-midsrvcs12:9092,ld-midsrvcs12:9093,ld-midsrvcs12:9094");
        System.setProperty("server.port", "20020");
        System.setProperty("spring.application.name", "embs-core");
        System.setProperty("spring.kafka.group-id", "embs");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldBuildGroupIdWithAppNameServerPortGroupIdClientId() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(APPLICATION_NAME, LOCAL_SERVER_PORT, GROUP_ID, CUSTOM_CLIENT_ID);
        assertEquals("esp-syslog_data_change", clientIdentifier.getGroupId());
    }

    @Test
    public void shouldBuildGroupIdWithAppNameGroupId() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(APPLICATION_NAME, null, GROUP_ID, null);
        assertEquals("esp-syslog_data_change", clientIdentifier.getGroupId());
    }

    @Test
    public void shouldBuildGroupIdWithAppName() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(APPLICATION_NAME, null, null, null);
        assertEquals("esp-syslog", clientIdentifier.getGroupId());
    }

    @Test
    public void shouldBuildGroupIdWithGroupId() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(null, null, GROUP_ID, null);
        assertEquals("data_change", clientIdentifier.getGroupId());
    }

    @Test
    public void shouldBuildGroupIdWithoutAppNameGroupId() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(null, null, null, null);
        assertEquals("ews-anonymous", clientIdentifier.getGroupId());
    }

    @Test
    public void shouldBuildClientIdWithAppNameServerPortGroupIdClientId() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(APPLICATION_NAME, LOCAL_SERVER_PORT, GROUP_ID, CUSTOM_CLIENT_ID);
        String expectedClientIdPrefix = "ng_esp-syslog_";

        assertEquals(expectedClientIdPrefix, clientIdentifier.getClientId().substring(0, expectedClientIdPrefix.length()));
    }

    @Test
    public void shouldBuildClientIdWithoutAppNameServerPortGroupIdClientId() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(null, null, null, null);
        String expectedClientIdPrefix = "ews-anonymous";

        assertEquals(expectedClientIdPrefix, clientIdentifier.getClientId().substring(0, expectedClientIdPrefix.length()));
    }

    @Test
    public void shouldBuildClientIdWithAppNameClientId() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(APPLICATION_NAME, null, null, CUSTOM_CLIENT_ID);
        String expectedClientIdPrefix = "ng_esp-syslog_";

        assertEquals(expectedClientIdPrefix, clientIdentifier.getClientId().substring(0, expectedClientIdPrefix.length()));
    }

    @Test
    public void shouldBuildClientIdWithServerPort() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(null, LOCAL_SERVER_PORT, null, null);
        String expectedClientIdPrefix = "ews-anonymous";

        assertEquals(expectedClientIdPrefix, clientIdentifier.getClientId().substring(0, expectedClientIdPrefix.length()));
    }

    @Test
    public void shouldBuildClientIdWithAppNameGroupIdClientId() throws Exception {

        ClientIdentifier clientIdentifier = new ClientIdentifier(APPLICATION_NAME, null, GROUP_ID, CUSTOM_CLIENT_ID);
        String expectedClientIdPrefix = "ng_esp-syslog_";

        assertEquals(expectedClientIdPrefix, clientIdentifier.getClientId().substring(0, expectedClientIdPrefix.length()));
    }
}