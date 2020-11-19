package net.securustech.ews.logger;

public class EWSLogContext {

    private static final ThreadLocal<EWSLog> ewsLog = new ThreadLocal();

    public static EWSLog getEWSLog() {
        return ewsLog.get();
    }

    public static void setEwsLog(EWSLog _ewsLog) {
        ewsLog.set(_ewsLog);
    }
}
