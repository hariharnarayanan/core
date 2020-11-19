package net.securustech.embs.send;

import net.securustech.embs.util.ClientIdentifier;
import net.securustech.embs.util.avro.AvroSerializer;
import net.securustech.embs.zookeeper.broker.BootstrapServers;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableAsync
public class KafkaProducerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Autowired
    private ClientIdentifier clientIdentifier;

    @Value("${spring.kafka.producer.max-block:20000}")
    private String maxBlock;

    @Value("${spring.kafka.producer.request-timeout:20000}")
    private String requestTimeout;

    @Value("${spring.kafka.producer.retries:0}")
    private String retries;

    @Value("${spring.kafka.producer.batch-size:16384}")
    private String batchSize;

    @Value("${spring.kafka.producer.linger:1}")
    private String linger;

    @Value("${spring.kafka.producer.buffer-memory:33554432}")
    private String bufferMemory;

    @Autowired
    private BootstrapServers bootstrapServers;

    @Bean
    public <T> ProducerFactory<String, T> producerFactory() {
        return new DefaultKafkaProducerFactory<String, T>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientIdentifier.getClientId());
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlock);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);


        return props;
    }

    @Bean
    public <T> KafkaTemplate<String, T> kafkaTemplate() {
        return new DelegatingKafkaTemplate<>(producerFactory());
    }

    private class DelegatingKafkaTemplate<K, V> extends KafkaTemplate<K, V> {
        private final Map<Class<?>, KafkaTemplate> kafkaTemplates = new HashMap<>();
        private final KafkaTemplate defaultKafkaTemplate;

        public DelegatingKafkaTemplate(ProducerFactory<K, V> defaultProducerFactory) {
            super(defaultProducerFactory);
            defaultKafkaTemplate = new KafkaTemplate(defaultProducerFactory);
        }

        @Override
        public ListenableFuture<SendResult<K, V>> send(String topic, Integer partition, K key, V data) {
            KafkaTemplate kafkaTemplate = defaultKafkaTemplate;
            if (data instanceof SpecificRecordBase) {
                Class<?> clazz = data.getClass();
                kafkaTemplate = kafkaTemplates.get(clazz);
                if (kafkaTemplate == null) {
                    ProducerFactory pf = new DefaultKafkaProducerFactory<>(producerConfigs(), new StringSerializer(), new AvroSerializer<>());
                    kafkaTemplate = new KafkaTemplate<K, V>(pf);
                    kafkaTemplates.put(clazz, kafkaTemplate);
                }
            }
            return kafkaTemplate.send(topic, partition, key, data);
        }
    }
}
