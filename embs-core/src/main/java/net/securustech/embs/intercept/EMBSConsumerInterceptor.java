package net.securustech.embs.intercept;


import net.securustech.embs.util.CorrelationId;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;


import static org.apache.commons.lang3.SerializationUtils.deserialize;

@Component
public class EMBSConsumerInterceptor implements ConsumerInterceptor<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMBSConsumerInterceptor.class);

    public EMBSConsumerInterceptor(){
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

    @Override
    public ConsumerRecords<String, String> onConsume(ConsumerRecords<String, String> records) {

        try {
            LOGGER.trace("Received Messages@COUNT :-> " + records.count());
            Long startTime = System.currentTimeMillis();
            records.forEach(record -> {
                LOGGER.trace("Received Message@KEY :-> " + record.key() + " : Partition :-> " + record.partition() + " : TOPIC :-> " + record.topic() + " : OFFSET :-> " + record.offset() + "\n[[[PAYLOAD]]] \n<<<" + record.value() + "\n");
                Headers headers = record.headers();
                String correlationId = null;
                if (null != headers) {
                    correlationId = headers.lastHeader(CorrelationId.UUID) != null ? (String) deserialize(headers.lastHeader(CorrelationId.UUID).value()) : null;
                }

                if (correlationId == null) {
                    correlationId = UUID.randomUUID().toString();
                    LOGGER.info("ESPUUIDInterceptor : NO UUID in Header : Generated :::>>> " + correlationId);
                } else {
                    LOGGER.info("EWSUUIDFilter : UUID FOUND in Header :::>>> " + correlationId);
                }
                CorrelationId.setId(correlationId);
            });
            Long duration = System.currentTimeMillis() - startTime;
            LOGGER.trace("Intercepted Messages@COUNT :-> " + records.count() + " ::: In [[[ " +  duration + " ]]]");
        } catch (Exception e) {
            LOGGER.error("Error Intercepting Messages@COUNT :-> " + records.count() + " ::: " + ExceptionUtils.getStackTrace(e));
        } finally {
            return  records;
        }
    }


    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
    }

    @Override
    public void close() {
    }

}
