package net.securustech.ews.service.types;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;

import static org.apache.commons.lang3.SerializationUtils.deserialize;

public class EWSRetryId {
    private static final ThreadLocal<String> ewsRetryId = new ThreadLocal<>();

    public static String getId() {
        return ewsRetryId.get();
    }

    public static void setId(String correlationId) {
        ewsRetryId.set(correlationId);
    }

    public static void parseRetryIdFromConsumerRecord(ConsumerRecord<String, String> consumerRecord) {
        Headers headers = consumerRecord.headers();
        String retryId = null;
        if (null != headers) {
            retryId = headers.lastHeader("retryId") != null ? (String) deserialize(headers.lastHeader("retryId").value()) : null;
        }
        EWSRetryId.setId(retryId);
    }

}
