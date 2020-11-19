package net.securustech.embs.stream;

import net.securustech.embs.EnableEmbsStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableEmbsStream
@SpringBootApplication
public class StreamApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Spring.Profiles.Active :-> " + System.getProperty("spring.profiles.active"));

        SpringApplication.run(StreamApplication.class, args);

        LOGGER.info("Started Enterprise Message Broker Service [EMBS] Stream...");
    }
}
