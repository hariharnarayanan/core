/*
 * Copyright (c) 2012 SECURUSTECH and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADERS
 */
package net.securustech.embs.send;

import net.securustech.embs.EmbsConstants;
import net.securustech.embs.util.ClientIdentifier;
import net.securustech.embs.util.EMBSHeaderConverter;
import net.securustech.embs.util.CorrelationId;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import static org.apache.commons.lang3.SerializationUtils.deserialize;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@EnableRetry
public class EMBSProducerImpl<T> implements EMBSProducer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMBSProducerImpl.class);

    @Autowired
    private KafkaService<T> kafkaService;

    @Autowired
    private ClientIdentifier clientIdentifier;

    @Autowired
    private EMBSHeaderConverter embsHeaderConverter;

    @Autowired
    private HttpServletRequest request;

    private SendResult<String, T> sendMessageToBroker(final String topic, final Integer partition, String key, final T message, HashMap<String, Object> rawHeaders) throws Exception {
        if(rawHeaders != null) {
            if( !rawHeaders.containsKey(CorrelationId.UUID)  || rawHeaders.get(CorrelationId.UUID) == null) {
                rawHeaders.put(CorrelationId.UUID, CorrelationId.getId());
            }
        } else {
            rawHeaders = new HashMap<String, Object>();
            rawHeaders.put(CorrelationId.UUID, CorrelationId.getId());
        }
        return kafkaService.sendWithHeaders(topic, partition, key, message, rawHeaders);
    }

    @Override
    public SendResult<String, T> sendRetryableMessage(ConsumerRecord<String, T> record, HashMap<String, Object> rawHeaders) throws Exception {

        Integer retryCount = 0;

        try {

            retryCount = (Integer) embsHeaderConverter.getHeaderValue(EmbsConstants.RETRY_COUNT_FIELD, record.headers());

            LOGGER.info("REQUEST Headers : RETRY_COUNT <<<" + retryCount + ">>>");

        } catch (Exception e) {
              LOGGER.info("REQUEST Headers : NO RETRY_COUNT, set RETRY_COUNT to <<<0>>>");
        }

        String topic = record.topic();
        long timestamp = System.currentTimeMillis();
        if (retryCount != null && retryCount < EmbsConstants.MAX_RETRY_COUNT) {
            retryCount++;
            LOGGER.info("REQUEST Payload \n<<<\n \t" + record.value() + "\n>>> \n Sending for <<<EXTERNAL_RETRY ::: " + retryCount + ">>>");
            //Send Message to Retryable Topic with HEADERS
            topic += EmbsConstants.RETRYABLE_TOPIC_SUFFIX;
        } else {

            LOGGER.info("REQUEST Payload \n<<<\n \t" + record.value() + "\n>>> \n MAXED OUT <<<EXTERNAL_RETRY ::: " + retryCount + ">>>");
            //Send Message to Non-Retryable Topic with HEADERS
            topic += EmbsConstants.NON_RETRYABLE_TOPIC_SUFFIX;
        }
        rawHeaders.put(EmbsConstants.RETRY_COUNT_FIELD, retryCount);
        return kafkaService.sendWithHeaders(topic, record.partition(), record.key(), record.value(), rawHeaders);
    }

    @Override
    public SendResult<String, T> sendMessageWithHeader(String topic, Integer partition, String key, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return sendMessageToBroker(topic, partition, key, message, rawHeaders);
    }

    @Override
    public SendResult<String, T> sendMessageWithHeader(String topic, Integer partition, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return sendMessageToBroker(topic, partition, null, message, rawHeaders);
    }

    @Override
    public SendResult<String, T> sendMessageWithHeader(String topic, String key, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return sendMessageToBroker(topic, null, key, message, rawHeaders);
    }

    @Override
    public SendResult<String, T> sendMessageWithHeader(String topic, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return sendMessageToBroker(topic, null, null, message, rawHeaders);
    }

    @Override
    @Async
    public SendResult<String, T> sendMessage(final String topic, final Integer partition, final String key, final T message) throws Exception {
        return sendMessageToBroker(topic, partition, key, message, null);
    }

    @Override
    @Async
    public SendResult<String, T> sendMessage(final String topic, final Integer partition, final T message) throws Exception {
        return sendMessageToBroker(topic, partition, null, message, null);
    }

    @Override
    @Async
    public SendResult<String, T> sendMessage(String topic, String key, T message) throws Exception {
        return sendMessageToBroker(topic, null, key, message, null);
    }

    @Override
    @Async
    public SendResult<String, T> sendMessage(final String topic, final T message) throws Exception {
        return sendMessageToBroker(topic, null, null, message, null);
    }

    @Override
    @Async
    public SendResult<String, T> sendMessage(ProducerRecord<String, T> producerRecord) throws Exception {
        return kafkaService.sendWithHeaders(producerRecord);
    }

    @Override
    public SendResult<String, T> sendMessageWithImplicitHeaders(String topic, Integer partition, String key, T message) throws Exception {
        return sendMessageToBroker(topic, partition, key, message, getRequestHeaders());
    }

    @Override
    public SendResult<String, T> sendNonRetryableMessageWithHeader(String topic, Integer partition, String key, T message, HashMap<String, Object> rawHeaders) throws Exception {
        if(rawHeaders != null) {
            if( !rawHeaders.containsKey(CorrelationId.UUID)  || rawHeaders.get(CorrelationId.UUID) == null) {
                rawHeaders.put(CorrelationId.UUID, CorrelationId.getId());
            }
        }
        return kafkaService.sendNonRetyableWithHeaders(topic, partition, key, message, rawHeaders);
    }

    private HashMap<String, Object> getRequestHeaders() {

        HashMap<String, Object> requestHeaders = new HashMap<>();
        Enumeration<String> enumeration =  request.getHeaderNames();

        while(enumeration.hasMoreElements()) {

            String headerName = enumeration.nextElement();
            String headerValue = request.getHeader(headerName);
            LOGGER.info("HttpRequest@Header :-> " + headerName + " : " + headerValue);
            if(! (headerName.equalsIgnoreCase("connection") || headerName.equalsIgnoreCase("origin")
                    || headerName.equalsIgnoreCase("user-agent") || headerName.equalsIgnoreCase("cache-control") || headerName.equalsIgnoreCase("content-type")
                    || headerName.equalsIgnoreCase("postman-token") || headerName.equalsIgnoreCase("accept") || headerName.equalsIgnoreCase("content-length")
                    || headerName.equalsIgnoreCase("accept-encoding") || headerName.equalsIgnoreCase("accept-language")
            )){
                requestHeaders.put(headerName, headerValue);
            }
        }

        requestHeaders.put("accessTime",new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ").format(new Date()));

        for (Object header : requestHeaders.keySet()) {

            LOGGER.info("HttpRequestMAP@Header :-> " + header + " : " + requestHeaders.get(header));
        }

        return requestHeaders;
    }
}
