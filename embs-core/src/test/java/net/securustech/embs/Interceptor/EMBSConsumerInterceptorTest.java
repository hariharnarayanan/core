package net.securustech.embs.Interceptor;

import net.securustech.embs.EmbsAutoConfiguration;
import net.securustech.embs.intercept.EMBSConsumerInterceptor;
import net.securustech.embs.util.CorrelationId;
import net.securustech.embs.util.EMBSHeaderConverter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static javafx.scene.input.KeyCode.V;
import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@ContextConfiguration(classes = EmbsAutoConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EMBSConsumerInterceptorTest {

    @Autowired
    EMBSConsumerInterceptor embsConsumerInterceptor;

    @Autowired
    EMBSHeaderConverter embsHeaderConverter;

    @BeforeClass
    public static void setUp() throws Exception {

        System.setProperty("spring.kafka.bootstrap-servers", "ld-midsrvcs12.lab.securustech.net:9092,ld-midsrvcs12.lab.securustech.net:9093,ld-midsrvcs12.lab.securustech.net:9094");
        System.setProperty("server.port", "20020");
        System.setProperty("spring.application.name", "embs-core");
        System.setProperty("spring.kafka.group-id", "embs");
    }

    @Test
    public void testInterceptorWithoutGenerateUUID(){

        HashMap<String, Object> rawHeaders = new HashMap<String, Object>();
        String correlationId = UUID.randomUUID().toString();
        rawHeaders.put(CorrelationId.UUID, correlationId);
        Iterable<Header> headers = embsHeaderConverter.buildHeaders(rawHeaders);
        String topic = "topic";
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord(topic, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, "key1", "value1", (Headers)headers);

        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();
        records.put(new TopicPartition(topic, 1), (List<ConsumerRecord<String, String>>)Arrays.asList(consumerRecord));

        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords(records);

        ConsumerRecords<String, String> consumerRecordsAfter = embsConsumerInterceptor.onConsume(consumerRecords);

        assertNotNull(consumerRecordsAfter);
        assertEquals(correlationId, CorrelationId.getId());

    }

    @Test
    public void testInterceptorGenerateUUID(){
        HashMap<String, Object> rawHeaders = new HashMap<String, Object>();
        String correlationId = UUID.randomUUID().toString();
        rawHeaders.put("Test", correlationId);
        Iterable<Header> headers = embsHeaderConverter.buildHeaders(rawHeaders);
        String topic = "topic";
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord(topic, 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, "key1", "value1", (Headers)headers);

        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();
        records.put(new TopicPartition(topic, 1), (List<ConsumerRecord<String, String>>)Arrays.asList(consumerRecord));

        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords( records);

        ConsumerRecords<String, String> consumerRecordsAfter = embsConsumerInterceptor.onConsume(consumerRecords);
        assertNotNull(consumerRecordsAfter);
        assertNotNull(CorrelationId.getId());
    }

    @Test
    public void testInterceptorException(){
        ConsumerRecords<String, String> consumerRecords = null;
        ConsumerRecords<String, String> consumerRecordsAfter = embsConsumerInterceptor.onConsume(consumerRecords);
        assertNull(consumerRecordsAfter);
    }

}
