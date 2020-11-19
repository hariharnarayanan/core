package net.securustech.embs.util;

public class CorrelationId {

    public static final String UUID = "uuid";
    public static final String CORRELATIONID = "correlationId";
    private static final ThreadLocal<String> ewsUUID = new ThreadLocal<>();

    public static String getId() {
        return ewsUUID.get();
    }

    public static void setId(String correlationId) {
        ewsUUID.set(correlationId);
    }
}