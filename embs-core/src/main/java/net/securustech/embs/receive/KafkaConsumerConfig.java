package net.securustech.embs.receive;

import lombok.Getter;
import net.securustech.embs.util.ClientIdentifier;
import net.securustech.embs.util.avro.AvroDeserializer;
import net.securustech.embs.zookeeper.broker.BootstrapServers;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Autowired
    private ClientIdentifier clientIdentifier;

    @Autowired
    private BootstrapServers bootstrapServers;

    @Value("${spring.kafka.consumer.enable-auto-commit:true}")
    private String enableAutoCommit;

    @Value("${spring.kafka.consumer.ack-mode:BATCH}")
    private ContainerProperties.AckMode ackMode;

    @Value("${spring.kafka.consumer.max-poll-records:500}")
    private Integer maxPollRecords;

    @Value("${spring.kafka.consumer.concurrency:3}")
    private Integer concurrency;

    @Value("${spring.kafka.consumer.auto-commit-interval:15000}")
    private Integer autoCommitInterval;

    @Value("${spring.kafka.consumer.session-timeout:60000}")
    private Integer sessionTimeout;

    @Value("${spring.kafka.consumer.auto-offset-reset:latest}")
    private String autoOffsetReset;

    private class DelegatingConcurrentKafkaListenerContainerFactory<K, V> extends AbstractKafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>, K, V> {
        private final Map<Class<?>, ConcurrentKafkaListenerContainerFactory> listernerContainerFactories = new HashMap<>();
        private final ConcurrentKafkaListenerContainerFactory defaultListenerContainerFactory;
        private ApplicationEventPublisher applicationEventPublisher;

        public DelegatingConcurrentKafkaListenerContainerFactory() {
            this(false);
        }

        public DelegatingConcurrentKafkaListenerContainerFactory(Boolean batchListener) {
            ConcurrentKafkaListenerContainerFactory factory =
                    new ConcurrentKafkaListenerContainerFactory();
            initListenerContainerFactory(factory);
            factory.setBatchListener(batchListener);
            this.defaultListenerContainerFactory = factory;
        }

        private void initListenerContainerFactory(ConcurrentKafkaListenerContainerFactory factory) {
            factory.setConsumerFactory(consumerFactory());
            factory.setConcurrency(concurrency);
            factory.getContainerProperties().setPollTimeout(3000);
            factory.getContainerProperties().setIdleEventInterval(60000L);
            factory.getContainerProperties().setAckMode(ackMode);
        }

        @Override
        public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            this.applicationEventPublisher = applicationEventPublisher;
            this.defaultListenerContainerFactory.setApplicationEventPublisher(applicationEventPublisher);
        }

        @Override
        protected ConcurrentMessageListenerContainer<K, V> createContainerInstance(KafkaListenerEndpoint endpoint) {
            return null;
        }

        @Override
        public ConcurrentMessageListenerContainer<K, V> createListenerContainer(KafkaListenerEndpoint endpoint) {
            ConcurrentKafkaListenerContainerFactory factory = this.defaultListenerContainerFactory;
            if (endpoint instanceof MethodKafkaListenerEndpoint) {
                MethodKafkaListenerEndpoint<K, V> methodKafkaListenerEndpoint = (MethodKafkaListenerEndpoint<K, V>)endpoint;
                Class<? extends SpecificRecordBase> avroType = findAvroType(methodKafkaListenerEndpoint.getMethod());
                if (avroType != null) {
                    factory = listernerContainerFactories.get(avroType);
                    if (factory == null) {
                        factory = new ConcurrentKafkaListenerContainerFactory();
                        initListenerContainerFactory(factory);
                        factory.setApplicationEventPublisher(this.applicationEventPublisher);
                        DefaultKafkaConsumerFactory consumerFactory = (DefaultKafkaConsumerFactory<K, V>) factory.getConsumerFactory();
                        consumerFactory.setValueDeserializer(new AvroDeserializer(avroType));
                    }
                }
            }

            return (ConcurrentMessageListenerContainer)factory.createListenerContainer(endpoint);
        }


        private Class<? extends SpecificRecordBase> findAvroType(Method method) {
            Class<? extends SpecificRecordBase> avroType = null;
            for(Class<?> clazz: method.getParameterTypes()) {
                if (clazz.getSuperclass() == SpecificRecordBase.class) {
                    avroType = (Class<? extends SpecificRecordBase>) clazz;
                    break;
                }
            }
            return avroType;
        }
    }

    @Bean
    public <T> KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, T>> kafkaListenerContainerFactory() {
        return new DelegatingConcurrentKafkaListenerContainerFactory<>();
    }

    @Bean
    public <T> KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, T>> batchFactory() {
        return new DelegatingConcurrentKafkaListenerContainerFactory<>(true);
    }

    public <T> ConsumerFactory<String, T> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers.getBootstrapServers());
        propsMap.put(ConsumerConfig.CLIENT_ID_CONFIG, clientIdentifier.getClientId());
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, clientIdentifier.getGroupId());
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        propsMap.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, Collections.singletonList(BalancedRangeAssignor.class));
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, "net.securustech.embs.intercept.EMBSConsumerInterceptor");

        return propsMap;
    }
}

