/*
 * Copyright (c) 2012 SECURUSTECH and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADERS
 */
package net.securustech.embs.consumer;

import net.securustech.embs.consumer.avro.User;
import net.securustech.embs.util.ClientIdentifier;
import net.securustech.embs.util.EMBSHeaderConverter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EMBSListener {

    @Autowired
    private ClientIdentifier clientIdentifier;

    @Autowired
    private EMBSHeaderConverter embsHeaderConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(EMBSListener.class);

    @KafkaListener(
            id = "#{clientIdentifier.getClientId()}",
            groupId = "#{clientIdentifier.getGroupId()}",
            topicPartitions = {
                    @TopicPartition(topic = "svv_visitors_upsert_test", partitions = {"0", "1", "2"})
            })
    public void process(ConsumerRecord<?, ?> record) throws IOException, ClassNotFoundException {

        LOGGER.info("EMBS@PROCESS@ConsumerRecord :-> " + record.value());
        LOGGER.info("EMBS@PROCESS@@KEY :-> " + record.key() + " : Partition :-> " + record.partition() + " : TOPIC :-> " + record.topic() + " : OFFSET :-> " + record.offset());

    }

    @KafkaListener(
            id = "#{clientIdentifier.getClientId()}_HEADERS",
            groupId = "#{clientIdentifier.getGroupId()}",
            topicPartitions = {
                    @TopicPartition(topic = "test_topic_hp", partitions = {"0", "1", "2"})
            })
    public void processWithHeaders(ConsumerRecord<?, ?> record) throws IOException, ClassNotFoundException {

        LOGGER.info("EMBS@PROCESS@HEADERS@ConsumerRecord " + record.value());
        LOGGER.info("EMBS@PROCESS@HEADERS@ConsumerRecord@Headers@RETRY_COUNT :-> " + embsHeaderConverter.getHeaderValue("RETRY_COUNT", record.headers()));
        LOGGER.info("EMBS@PROCESS@HEADERS@ConsumerRecord@Headers@ACTION :-> " + embsHeaderConverter.getHeaderValue("ACTION", record.headers()));
        LOGGER.info("EMBS@PROCESS@HEADERS@KEY :-> " + record.key() + " : Partition :-> " + record.partition() + " : TOPIC :-> " + record.topic() + " : OFFSET :-> " + record.offset());

    }

    @KafkaListener(
//            id = "#{clientIdentifier.getClientId()}_HEADERS_PARAMS",
//            group = "#{clientIdentifier.getGroupId()}",
            topics = "ews_kube_test")
    public void processWithHeadersAsParameters(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.OFFSET) String offset) {

        LOGGER.info("EMBS@PROCESS@HEADERS@PARAMETERS@Payload :-> " + message);
        LOGGER.info("EMBS@PROCESS@HEADERS@PARAMETERS@KEY :-> " + key + " : Partition :-> " + partition + " : TOPIC :-> " + topic + " : OFFSET :-> " + offset);

    }

    @KafkaListener(id = "avro", topics="avro_store")
    public void processAvro(User user,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.OFFSET) String offset) {

        LOGGER.info("EMBS@PROCESS@AVRO@User :-> " + user);
        LOGGER.info("EMBS@PROCESS@AVRO@KEY :-> " + key + " : Partition :-> " + partition + " : TOPIC :-> " + topic + " : OFFSET :-> " + offset);
    }
}
