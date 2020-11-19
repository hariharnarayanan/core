package net.securustech.ews.data.elasticsearch.rest.autoconfigure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.MalformedParametersException;
import java.util.stream.Stream;

import static org.elasticsearch.client.RestClient.builder;

@Configuration
@ConditionalOnClass({RestClient.class, RestHighLevelClient.class})
@ConditionalOnProperty(
        prefix = "spring.data.elasticsearch.rest",
        name = {"uri"},
        matchIfMissing = false
)
@EnableConfigurationProperties({ElasticsearchRestProperties.class, ElasticsearchProperties.class})
public class ElasticsearchRestAutoConfiguration {

    @Value("${spring.data.elasticsearch.rest.maxRetryTimeoutMillis:90000}")
    private Integer maxRetryTimeout;

    private static final Log logger = LogFactory.getLog(ElasticsearchRestAutoConfiguration.class);

    private RestHighLevelClient highLevelClient;

    @Autowired
    private ElasticsearchRestProperties elasticsearchRestProperties;

    @Autowired(required = false)
    private ElasticsearchProperties elasticsearchProperties;

    @Bean
    @ConditionalOnMissingBean(RestClient.class)
    public RestClientBuilder restClientBuilder() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticsearchRestProperties.getUsername(), elasticsearchRestProperties.getPassword()));
        RestClientBuilder builder = builder((HttpHost[])Stream.of(elasticsearchRestProperties.getUri().split(",")).map(node -> {
            String[] hostPort = node.split(":");
            if (hostPort.length != 3) throw new MalformedParametersException("Elasticsearch uri format error");
            return new HttpHost(hostPort[1].substring(2), Integer.parseInt(hostPort[2]), hostPort[0]);
        }).toArray(size -> new HttpHost[size]))
        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder
                                .setConnectTimeout(elasticsearchRestProperties.getConnectTimeout())
                                .setSocketTimeout(elasticsearchRestProperties.getSocketTimeout()));
        return builder;

    }

    @Bean
    public RestClient restClient() {
        return restClientBuilder().build();
    }

    @Bean
    @ConditionalOnMissingBean(RestHighLevelClient.class)
    public RestHighLevelClient restHighLevelClient(RestClientBuilder restClientBuilder) {
        this.highLevelClient = new RestHighLevelClient(restClientBuilder);
        return this.highLevelClient;
    }
}
