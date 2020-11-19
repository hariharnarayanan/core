package net.securustech.ews.util.jmx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.management.MBeanServer;

@Configuration
public class DataSourceMBeanConfiguration {

    private final ObjectMapper objectMapper;

    private final DataSourceMetrics metrics;

    private Environment environment;

    public DataSourceMBeanConfiguration(ObjectMapper objectMapper, DataSourceMetrics metrics) {
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @Bean
    public DataSourceMBeanExporter dataSourceMBeanExporter(MBeanServer server) {
        DataSourceMBeanExporter mBeanExporter = new DataSourceMBeanExporter(metrics, objectMapper);
        mBeanExporter.setServer(server);
        mBeanExporter.setEnsureUniqueRuntimeObjectNames(false);
        return mBeanExporter;
    }

    @Bean
    @ConditionalOnMissingBean(MBeanServer.class)
    public MBeanServer mBeanServer() {
        return new JmxAutoConfiguration(environment).mbeanServer();
    }
}
