package net.securustech.embs.send;

import net.securustech.embs.util.EMBSHeaderConverter;
import net.securustech.embs.util.EMBSRetryableException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static net.securustech.embs.util.EMBSErrorMessages.SEND_TO_BROKER_FAILED;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class KafkaService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private KafkaTemplate<String, T> kafkaTemplate;

    @Autowired
    private EMBSHeaderConverter embsHeaderConverter;

    @Retryable(value = EMBSRetryableException.class, maxAttempts = 4, backoff = @Backoff(delay = 5000L, maxDelay = 30000L, multiplier = 2.0D))
    public SendResult<String, T> send(final String topic, final Integer partition, final String key, final T message) throws Exception {
        return this.sendWithHeaders(topic, partition, key, message, null);
    }

    @Retryable(value = EMBSRetryableException.class, maxAttempts = 4, backoff = @Backoff(delay = 5000L, maxDelay = 30000L, multiplier = 2.0D))
    public SendResult<String, T> sendWithHeaders(final String topic, final Integer partition, final String key, final T message, final HashMap<String, Object> rawHeaders) throws Exception {
        return send(topic, partition, key, message, rawHeaders);
    }

    @Retryable(value = EMBSRetryableException.class, maxAttempts = 4, backoff = @Backoff(delay = 5000L, maxDelay = 30000L, multiplier = 2.0D))
    public SendResult<String, T> sendWithHeaders(ProducerRecord<String, T> producerRecord) throws Exception {
        try {
            return kafkaTemplate.send(producerRecord).get(5, TimeUnit.SECONDS);
        } catch(Exception e) {

            LOGGER.info("FAILED@SendMessage@Exception >>> " + ExceptionUtils.getStackTrace(e));

            LOGGER.info("FAILED@SendMessage@KEY :-> " + producerRecord.key() + " : Partition :-> " + producerRecord.partition() + " : TOPIC :-> " + producerRecord.topic() + " : Message :-> " + producerRecord.value() + " >>> Retrying Now >>>");
            throw new EMBSRetryableException(e.getMessage(), SEND_TO_BROKER_FAILED.getErrorCode(), SEND_TO_BROKER_FAILED.getErrorMessage());
        }
    }

    public SendResult<String, T> sendNonRetyableWithHeaders(final String topic, final Integer partition, final String key, final T message, final HashMap<String, Object> rawHeaders) throws Exception {
        return send(topic, partition, key, message, rawHeaders);
    }

    private SendResult<String, T> send(String topic, Integer partition, String key, T message, HashMap<String, Object> rawHeaders) throws EMBSRetryableException {
        try {
            ProducerRecord<String, T> producerRecord = new ProducerRecord<>(topic, partition, key, message, embsHeaderConverter.buildHeaders(rawHeaders));
            return this.sendWithHeaders(producerRecord);
        } catch(Exception e) {

            LOGGER.info("FAILED@SendMessage@Exception >>> " + ExceptionUtils.getStackTrace(e));

            LOGGER.info("FAILED@SendMessage@KEY :-> " + key + " : Partition :-> " + partition + " : TOPIC :-> " + topic + " : Message :-> " + message + " >>> Retrying Now >>>");
            throw new EMBSRetryableException(e.getMessage(), SEND_TO_BROKER_FAILED.getErrorCode(), SEND_TO_BROKER_FAILED.getErrorMessage());
        }
    }


}
