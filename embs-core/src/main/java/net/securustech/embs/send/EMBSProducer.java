package net.securustech.embs.send;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.support.SendResult;

import java.util.HashMap;

public interface EMBSProducer<T> {

    SendResult<String, T> sendMessageWithHeader(String topic, Integer partition, String key, T message, HashMap<String, Object> rawHeaders) throws Exception;
    SendResult<String, T> sendMessageWithHeader(String topic, Integer partition, T message, HashMap<String, Object> rawHeaders) throws Exception;
    SendResult<String, T> sendMessageWithHeader(String topic, String key, T message, HashMap<String, Object> rawHeaders) throws Exception;
    SendResult<String, T> sendMessageWithHeader(String topic, T message, HashMap<String, Object> rawHeaders) throws Exception;

    SendResult<String, T> sendMessage(final String topic, final Integer partition, final String key, final T message) throws Exception;
    SendResult<String, T> sendMessage(final String topic, final Integer partition, final T message) throws Exception;
    SendResult<String, T> sendMessage(final String topic, final String key, final T message) throws Exception;
    SendResult<String, T> sendMessage(final String topic, final T message) throws Exception;

    SendResult<String, T> sendMessage(ProducerRecord<String, T> producerRecord) throws Exception;

    SendResult<String, T> sendRetryableMessage(final ConsumerRecord<String, T> record, HashMap<String, Object> rawHeaders) throws Exception;
    
    SendResult<String, T> sendMessageWithImplicitHeaders(String topic, Integer partition, String key, T message) throws Exception;

    default SendResult<String, T> sendMessageWithImplicitHeaders(String topic, Integer partition, T message) throws Exception {
        return sendMessageWithImplicitHeaders(topic, partition, null, message);
    }

    default SendResult<String, T> sendMessageWithImplicitHeaders(String topic, String key, T message) throws Exception {
        return sendMessageWithImplicitHeaders(topic, null, key, message);
    }

    default SendResult<String, T> sendMessageWithImplicitHeaders(String topic, T message) throws Exception {
        return sendMessageWithImplicitHeaders(topic, null, null, message);
    }
    SendResult<String, T> sendNonRetryableMessageWithHeader(String topic, Integer partition, String key, T message, HashMap<String, Object> rawHeaders) throws Exception;
}
