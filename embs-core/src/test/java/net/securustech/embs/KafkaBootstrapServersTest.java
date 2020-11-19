package net.securustech.embs;

import net.securustech.embs.zookeeper.broker.BootstrapServers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = EmbsAutoConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class KafkaBootstrapServersTest {

    @Autowired
    private EmbsAutoConfiguration embsAutoConfiguration;

    @Autowired
    private BootstrapServers bootstrapServers;

    @BeforeClass
    public static void setUp() throws Exception {

        System.setProperty("spring.kafka.bootstrap-servers", "ld-midsrvcs12.lab.securustech.net:9092,ld-midsrvcs12.lab.securustech.net:9093,ld-midsrvcs12.lab.securustech.net:9094");
        System.setProperty("server.port", "20020");
        System.setProperty("spring.application.name", "embs-core");
        System.setProperty("spring.kafka.group-id", "embs");
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void shouldRetrieveKafkaServersFromKafkaBootstrapServers() throws Exception {

        BootstrapServers kafkaBrokers = embsAutoConfiguration.retrieveKafkaBrokerHosts();
        System.out.println("Bootstrap Servers :--> " + kafkaBrokers.getBootstrapServers());
        System.out.println("Injected Bootstrap Servers :--> " + bootstrapServers.getBootstrapServers());

        assertNotNull(kafkaBrokers);
        assertEquals("ld-midsrvcs12.lab.securustech.net:9092,ld-midsrvcs12.lab.securustech.net:9093,ld-midsrvcs12.lab.securustech.net:9094", kafkaBrokers.getBootstrapServers());
    }
}