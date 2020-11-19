package net.securustech.embs.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import net.securustech.embs.EnableEmbsProducer;

@EnableEmbsProducer
@SpringBootApplication
public class EMBSProducerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMBSProducerApplication.class);

    public static void main(String[] args) {

        LOGGER.info("Spring.Profiles.Active :-> " + System.getProperty("spring.profiles.active"));

        SpringApplication.run(EMBSProducerApplication.class, args);

        LOGGER.info("Started Enterprise Message Broker Service [EMBS] Producer...");
    }
}
