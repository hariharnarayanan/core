package net.securustech.ews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.embs.send.EMBSProducer;
import net.securustech.ews.core.repository.entity.ExternalSystemRetryLog;
import net.securustech.ews.core.repository.entity.RetryStatus;
import net.securustech.ews.exception.entities.EWSException;
import net.securustech.ews.service.types.EMBSTopic;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
public class EMBSPublisherService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMBSPublisherService.class);

    @Autowired
    private EMBSProducer publisher;

    @Autowired
    private ExternalSystemRetryLogService externalSystemRetryLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.application.name:anonymous}")
    private String applicationName;

    @Value("${ews.retry.count.max:0}")
    private Short ewsRetryCountMax;

    public SendResult<String, T> publish(String topic, Integer partition, String key, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return embsPublish(topic, partition, key, message, rawHeaders, false);
    }

    public SendResult<String, T> durablePublish(String topic, Integer partition, String key, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return embsPublish(topic, partition, key, message, rawHeaders, true);
    }

    public SendResult<String, T> publish(String topic, Integer partition, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return embsPublish(topic, partition, null, message, rawHeaders, false);
    }

    public SendResult<String, T> durablePublish(String topic, Integer partition, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return embsPublish(topic, partition, null, message, rawHeaders, true);
    }

    public SendResult<String, T> publish(String topic, String key, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return embsPublish(topic, null, key, message, rawHeaders, false);
    }

    public SendResult<String, T> durablePublish(String topic, String key, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return embsPublish(topic, null, key, message, rawHeaders, true);
    }

    public SendResult<String, T> publish(String topic, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return embsPublish(topic, null, null, message, rawHeaders, false);
    }

    public SendResult<String, T> durablePublish(String topic, T message, HashMap<String, Object> rawHeaders) throws Exception {
        return embsPublish(topic, null, null, message, rawHeaders, true);
    }

    // NOTE: THIS IS INVOKED ONLY BY THE RETRY TIMER AND IS A NON DURABLE PUBLISH.
    public SendResult<String, T> publish(final ExternalSystemRetryLog externalSystemRetryLog) throws Exception {

        LOGGER.info("PUBLISH@MESSAGE@START on Topic [[[ " + externalSystemRetryLog.getTopic() + " ]]] :::-> MESSAGE ::: \n<<<\n" + externalSystemRetryLog.getPayload() + "\n>>>\n");

        SendResult<String, T> response = null;

        try {

            HashMap<String, Object> rawHeaders = null;
            if (StringUtils.isNotBlank(externalSystemRetryLog.getHttpHeaders())) {
                rawHeaders = objectMapper.readValue(externalSystemRetryLog.getHttpHeaders(), HashMap.class);
            } else  if(rawHeaders == null) {
                rawHeaders = new HashMap<String, Object>();
            }

            rawHeaders.put("retryId", externalSystemRetryLog.getRetryId().toString());

            response = publisher.sendNonRetryableMessageWithHeader(externalSystemRetryLog.getTopic(), 0, externalSystemRetryLog.getKey(), externalSystemRetryLog.getPayload(), rawHeaders);

            LOGGER.info("PUBLISH@MESSAGE@DONE :::-> " + response);

        } catch (Exception e) {
            LOGGER.error("PUBLISH@MESSAGE@FAILED :::-> " + ExceptionUtils.getStackTrace(e), e);
        }
        return response;
    }

    private SendResult<String, T> embsPublish(final String topic, final Integer partition, final String key, final T messagePayload, HashMap<String, Object> rawHeaders, boolean durable) throws Exception {

        LOGGER.info("PUBLISH@MESSAGE@START on Topic [[[ " + topic + " ]]] :::-> MESSAGE ::: \n<<<\n" + messagePayload + "\n>>>\n");

        SendResult<String, T> response = null;

        try {

            response = publisher.sendMessageWithHeader(topic, partition, key, messagePayload, rawHeaders);

            LOGGER.info("PUBLISH@MESSAGE@DONE :::-> " + response);

        } catch (Exception e) {

            LOGGER.error("PUBLISH@MESSAGE@FAILED :::-> " + ExceptionUtils.getStackTrace(e));
            if (durable) {

                saveEmbsAttemptInDB(new EMBSTopic(topic, key), messagePayload.toString(), e);
            } else {

                throw e;
            }
        }

        return response;
    }

    public SendResult<String, T> publish(final String topic, final Integer partition, final String key, final T message) throws Exception {
        return embsPublish(topic, partition, key, message, false);
    }

    public SendResult<String, T> durablePublish(final String topic, final Integer partition, final String key, final T message) throws Exception {
        return embsPublish(topic, partition, key, message, true);
    }

    public SendResult<String, T> publish(final String topic, final Integer partition, final T message) throws Exception {
        return embsPublish(topic, partition, null, message, false);
    }

    public SendResult<String, T> durablePublish(final String topic, final Integer partition, final T message) throws Exception {
        return embsPublish(topic, partition, null, message, true);
    }

    public SendResult<String, T> publish(String topic, String key, T message) throws Exception {
        return embsPublish(topic, null, key, message, false);
    }

    public SendResult<String, T> durablePublish(String topic, String key, T message) throws Exception {
        return embsPublish(topic, null, key, message, true);
    }

    public SendResult<String, T> publish(final String topic, final T message) throws Exception {
        return embsPublish(topic, null, null, message, false);
    }

    public SendResult<String, T> durablePublish(final String topic, final T message) throws Exception {
        return embsPublish(topic, null, null, message, true);
    }

    private SendResult<String, T> embsPublish(final String topic, final Integer partition, final String key, final T messagePayload, boolean durable) throws Exception {

        LOGGER.info("PUBLISH@MESSAGE@START on Topic [[[ " + topic + " ]]] :::-> MESSAGE ::: \n<<<\n" + messagePayload + "\n>>>\n");

        SendResult<String, T> response = null;

        try {

            response = publisher.sendMessage(topic, partition, key, messagePayload);

            LOGGER.info("PUBLISH@MESSAGE@DONE :::-> " + response);

        } catch (Exception e) {

            LOGGER.error("PUBLISH@MESSAGE@FAILED :::-> " + ExceptionUtils.getStackTrace(e));
            if (durable) {

                saveEmbsAttemptInDB(new EMBSTopic(topic, key), messagePayload.toString(), e);
            } else {

                throw e;
            }
        }

        return response;
    }

    public SendResult<String, T> publish(ProducerRecord<String, T> producerRecord) throws Exception {
        return embsPublish(producerRecord, false);
    }

    public SendResult<String, T> durablePublish(ProducerRecord<String, T> producerRecord) throws Exception {

        return embsPublish(producerRecord, true);
    }

    private SendResult<String, T> embsPublish(ProducerRecord<String, T> producerRecord, boolean durable) throws Exception {

        LOGGER.info("PUBLISH@MESSAGE@START on Topic [[[ " + producerRecord.topic() + " ]]] :::-> MESSAGE ::: \n<<<\n" + producerRecord.value() + "\n>>>\n");

        SendResult<String, T> response = null;

        try {

            response = publisher.sendMessage(producerRecord);

            LOGGER.info("PUBLISH@MESSAGE@DONE :::-> " + response);

        } catch (Exception e) {

            LOGGER.error("PUBLISH@MESSAGE@FAILED :::-> " + ExceptionUtils.getStackTrace(e));
            if (durable) {

                saveEmbsAttemptInDB(new EMBSTopic(producerRecord.topic(), producerRecord.key()), producerRecord.value().toString(), e);
            } else {

                throw e;
            }
        }

        return response;
    }

    public SendResult<String, String> publish(EMBSTopic topic, String messagePayload) throws Exception {

        return embsPublish(topic, messagePayload, false);
    }

    public SendResult<String, String> durablePublish(EMBSTopic topic, String messagePayload) throws Exception {

        return embsPublish(topic, messagePayload, true);
    }

    private SendResult<String, String> embsPublish(EMBSTopic topic, String messagePayload, boolean durable) throws Exception {

        LOGGER.info("PUBLISH@MESSAGE@START on Topic [[[ " + topic.getTopic() + " ]]] :::-> MESSAGE ::: \n<<<\n" + messagePayload + "\n>>>\n");

        SendResult<String, String> response = null;

        try {

            response = publisher.sendMessageWithImplicitHeaders(topic.getTopic(), topic.getKey(), messagePayload);

            LOGGER.info("PUBLISH@MESSAGE@DONE :::-> " + response);

        } catch (Exception e) {

            LOGGER.error("PUBLISH@MESSAGE@FAILED :::-> " + ExceptionUtils.getStackTrace(e));
            if (durable) {

                saveEmbsAttemptInDB(topic, messagePayload, e);
            } else {

                throw e;
            }
        }

        return response;
    }

    private void saveEmbsAttemptInDB(final EMBSTopic topic, final String messagePayload, Exception e) throws EWSException {

        try {

            externalSystemRetryLogService.save(topic, messagePayload);

        } catch (EWSException e1) {

            LOGGER.error("EXTERNAL@RETRY@EMBS@LOG@FAILED :::-> " + ExceptionUtils.getStackTrace(e1));
            e1.addSuppressed(e);
            throw e1;
        }
    }

    private void saveEmbsAttemptInDB(final ExternalSystemRetryLog externalSystemRetryLog) throws EWSException {

        try {

            externalSystemRetryLogService.save(externalSystemRetryLog);

        } catch (EWSException e1) {

            LOGGER.error("EXTERNAL@RETRY@EMBS@LOG@FAILED :::-> " + ExceptionUtils.getStackTrace(e1));
            throw e1;
        }
    }
}