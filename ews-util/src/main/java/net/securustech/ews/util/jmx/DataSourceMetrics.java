package net.securustech.ews.util.jmx;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.*;

@Configuration
public class DataSourceMetrics {
    private static final String DATASOURCE_SUFFIX = "dataSource";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Collection<DataSourcePoolMetadataProvider> providers;

    private final Map<String, DataSourcePoolMetadata> metadataByPrefix = new HashMap<String, DataSourcePoolMetadata>();

    @PostConstruct
    public void initialize() {
        DataSource primaryDataSource = getPrimaryDataSource();
        DataSourcePoolMetadataProvider provider = new DataSourcePoolMetadataProviders(
                this.providers);
        for (Map.Entry<String, DataSource> entry : this.applicationContext
                .getBeansOfType(DataSource.class).entrySet()) {
            String beanName = entry.getKey();
            DataSource bean = entry.getValue();
            String prefix = createPrefix(beanName, bean, bean.equals(primaryDataSource));
            DataSourcePoolMetadata poolMetadata = provider
                    .getDataSourcePoolMetadata(bean);
            if (poolMetadata != null) {
                this.metadataByPrefix.put(prefix, poolMetadata);
            }
        }
    }

    public Collection<Metric<?>> metrics() {
        Set<Metric<?>> metrics = new LinkedHashSet<Metric<?>>();
        for (Map.Entry<String, DataSourcePoolMetadata> entry : this.metadataByPrefix
                .entrySet()) {
            String prefix = entry.getKey();
            prefix = (prefix.endsWith(".") ? prefix : prefix + ".");
            DataSourcePoolMetadata metadata = entry.getValue();
            addMetric(metrics, prefix + "active", metadata.getActive());
            addMetric(metrics, prefix + "usage", metadata.getUsage());
        }
        return metrics;
    }

    private <T extends Number> void addMetric(Set<Metric<?>> metrics, String name, T value) {
        if (value != null) {
            metrics.add(new Metric<T>(name, value));
        }
    }

    protected String createPrefix(String name, DataSource dataSource, boolean primary) {
        if (primary) {
            return "datasource.primary";
        }
        if (name.length() > DATASOURCE_SUFFIX.length()
                && name.toLowerCase().endsWith(DATASOURCE_SUFFIX.toLowerCase())) {
            name = name.substring(0, name.length() - DATASOURCE_SUFFIX.length());
        }
        return "datasource." + name;
    }

    private DataSource getPrimaryDataSource() {
        try {
            return this.applicationContext.getBean(DataSource.class);
        }
        catch (NoSuchBeanDefinitionException ex) {
            return null;
        }
    }

    public static class DataSourcePoolMetadataProviders implements DataSourcePoolMetadataProvider {

        private final List<DataSourcePoolMetadataProvider> providers;

        /**
         * Create a {@link DataSourcePoolMetadataProviders} instance with an initial
         * collection of delegates to use.
         * @param providers the data source pool metadata providers
         */
        public DataSourcePoolMetadataProviders(
                Collection<? extends DataSourcePoolMetadataProvider> providers) {
            this.providers = (providers == null
                    ? Collections.<DataSourcePoolMetadataProvider>emptyList()
                    : new ArrayList<DataSourcePoolMetadataProvider>(providers));
        }

        @Override
        public DataSourcePoolMetadata getDataSourcePoolMetadata(DataSource dataSource) {
            for (DataSourcePoolMetadataProvider provider : this.providers) {
                DataSourcePoolMetadata metadata = provider
                        .getDataSourcePoolMetadata(dataSource);
                if (metadata != null) {
                    return metadata;
                }
            }
            return null;
        }

    }

}
