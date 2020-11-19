package net.securustech.embs;

public class EmbsConstants {
    public static final String RETRYABLE_TOPIC_SUFFIX = "_RETRYABLE";
    public static final String NON_RETRYABLE_TOPIC_SUFFIX = "_NONRETRYABLE";
    public static final int MAX_RETRY_COUNT = 3;
    public static final String RETRY_COUNT_FIELD = "RETRY_COUNT";
    public static final String HEADER_KEY_USERNAME = "username";
}
