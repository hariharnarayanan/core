package net.securustech.ews.util.jmx;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class DataSourceMBean {
    private final DataConverter dataConverter;

    private final DataSourceMetrics metrics;

    public DataSourceMBean(String beanName, DataSourceMetrics metrics, ObjectMapper objectMapper) {
        this.dataConverter = new DataConverter(objectMapper);
        this.metrics = metrics;
    }

    public Object getData() {
        return this.dataConverter.convert(this.invoke());
    }

    private Map<String, Object> invoke() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            for (Metric<?> metric : metrics.metrics()) {
                result.put(metric.getName(), metric.getValue());
            }
        } catch(Exception ex) {
            // Could not evaluate metrics
        }
        return result;
    }
}
