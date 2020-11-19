package net.securustech.ews.data.elasticsearch.rest.autoconfigure;

import net.securustech.ews.data.elasticsearch.rest.ElasticsearchRestTemplate;
import net.securustech.ews.data.elasticsearch.rest.extension.CustomizedResultMapper;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

@Configuration
@ConditionalOnClass({RestHighLevelClient.class, ElasticsearchRestTemplate.class})
@AutoConfigureAfter(ElasticsearchRestAutoConfiguration.class)
public class ElasticsearchRestDataAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RestHighLevelClient.class)
    public ElasticsearchRestTemplate elasticsearchTemplate(RestHighLevelClient client, ElasticsearchConverter converter, CustomizedResultMapper resultMapper) {
        return new ElasticsearchRestTemplate(client, converter, resultMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext mappingContext) {
        return new MappingElasticsearchConverter(mappingContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomizedResultMapper resultMapper(SimpleElasticsearchMappingContext mappingContext) {
        return new CustomizedResultMapper(mappingContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleElasticsearchMappingContext mappingContext() {
        return new SimpleElasticsearchMappingContext();
    }
}
