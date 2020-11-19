package net.securustech.embs;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.embs.intercept.EMBSConsumerInterceptor;
import net.securustech.embs.util.ClientIdentifier;
import net.securustech.embs.util.EMBSHeaderConverter;
import net.securustech.embs.zookeeper.broker.BootstrapServers;
import net.securustech.embs.zookeeper.broker.BrokerInfo;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.io.IOException;
import java.util.List;

import static net.securustech.embs.zookeeper.ZookeeperConstants.ZOOKEEPER_PATH_KAFKA_BROKERS;
import static net.securustech.embs.zookeeper.ZookeeperConstants.ZOOKEEPR_PATH_SEPARATOR;

@Configuration
@ConditionalOnClass(EnableKafka.class)
public class EmbsAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbsAutoConfiguration.class);

    @Value("${spring.cloud.zookeeper.connect-string:}")
    private String zookeeperServers;

    @Value("${spring.kafka.bootstrap-servers:}")
    private String kafkaBootstrapServers;

    @Value("${server.port:}")
    private String localServerPort;

    @Value("${spring.application.name:}")
    private String applicationName;

    @Value("${spring.kafka.group-id:}")
    private String groupId;

    @Value("${spring.kafka.client-id:}")
    private String clientId;

    @Bean
    public ClientIdentifier clientIdentifier() {

        return new ClientIdentifier(applicationName, localServerPort, groupId, clientId);
    }

    @Bean
    public EMBSConsumerInterceptor embsConsumerInterceptor() {
        return new EMBSConsumerInterceptor();
    }

    @Bean
    public EMBSHeaderConverter embsHeaderConverter() {
        return new EMBSHeaderConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperServers, retryPolicy);
        client.start();
        return client;
    }

    @Bean
    public BootstrapServers retrieveKafkaBrokerHosts() throws IOException, KeeperException, InterruptedException {

        LOGGER.debug("START@RETRIEVE@EMBS@Bootstrap@Servers :::>>> ZookeeperServers :::>>> " + zookeeperServers + " :::>>> BootstrapServers :::>>> " + kafkaBootstrapServers);

        BootstrapServers bootstrapServers = new BootstrapServers(kafkaBootstrapServers);
        StringBuffer bootstrapServerString = new StringBuffer();

        ObjectMapper objectMapper = new ObjectMapper();

        if (zookeeperServers != null && zookeeperServers.length() > 0) {

            LOGGER.debug("EMBS@Zookeeper@Servers NOT NULL :::>>> " + zookeeperServers + " <<<:::");

            ZooKeeper zooKeeper = new ZooKeeper(zookeeperServers, 10000,
                    event -> LOGGER.debug("EMBS@Zookeeper ::: Encountered Status Change :::>>> " + event.getState().name()));

            List<String> kafkaBrokerIds = zooKeeper.getChildren(ZOOKEEPER_PATH_KAFKA_BROKERS, false);

            kafkaBrokerIds.stream().forEach(e -> {
                try {

                    bootstrapServerString.append(buildKafkaBroker(zooKeeper, e, objectMapper));
                    bootstrapServers.setBootstrapServers(bootstrapServerString.substring(0, bootstrapServerString.length() - 1));
                } catch (Exception e1) {

                    LOGGER.error(ExceptionUtils.getStackTrace(e1));
                    bootstrapServers.setBootstrapServers(kafkaBootstrapServers);
                }
            });
        }

        LOGGER.debug("DONE@RETRIEVE@EMBS@Bootstrap@Servers :::>>> " + bootstrapServers.getBootstrapServers() + " <<<:::");

        return bootstrapServers;
    }

    private String buildKafkaBroker(ZooKeeper zooKeeper, String kafkaBrokerId, ObjectMapper objectMapper) throws KeeperException, InterruptedException, IOException {

        String kafkaBroker = new String(zooKeeper.getData(ZOOKEEPER_PATH_KAFKA_BROKERS + ZOOKEEPR_PATH_SEPARATOR + kafkaBrokerId, false, null));
        BrokerInfo brokerInfo = objectMapper.readValue(kafkaBroker, BrokerInfo.class);

        return brokerInfo.getHost() + ":" + brokerInfo.getPort() + ",";
    }
}
