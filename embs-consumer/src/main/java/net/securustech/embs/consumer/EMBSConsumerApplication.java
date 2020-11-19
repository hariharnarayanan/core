package net.securustech.embs.consumer;

import net.securustech.embs.EnableEmbsConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableEmbsConsumer
@SpringBootApplication
public class EMBSConsumerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMBSConsumerApplication.class);

    public static void main(String[] args) {

        LOGGER.info("Spring.Profiles.Active :-> " + System.getProperty("spring.profiles.active"));

        SpringApplication.run(EMBSConsumerApplication.class, args);

        LOGGER.info("Started Enterprise Message Broker Service [EMBS] Consumer...");
    }
}
