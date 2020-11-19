package net.securustech.ews.util.jmx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.ObjectNameManager;

import javax.annotation.PostConstruct;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class DataSourceMBeanExporter extends MBeanExporter {
    private final DataSourceMetrics metrics;

    private final ObjectMapper objectMapper;

    public DataSourceMBeanExporter(DataSourceMetrics metrics, ObjectMapper objectMapper) {
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initilize() {
        DataSourceMBean dataSourceMBean = new DataSourceMBean("DataSourceMBean", metrics, objectMapper);
        registerManagedResource(dataSourceMBean);
    }

    @Override
    protected ObjectName getObjectName(Object bean, String beanKey) throws MalformedObjectNameException {
        StringBuilder builder = new StringBuilder();
        builder.append("org.springframework.boot");
        builder.append(":type=Endpoint");
        builder.append(",name=Metrics");
        return ObjectNameManager.getInstance(builder.toString());
    }
}
