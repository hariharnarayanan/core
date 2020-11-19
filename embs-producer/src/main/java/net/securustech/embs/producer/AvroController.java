package net.securustech.embs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.embs.producer.avro.User;
import net.securustech.embs.send.EMBSProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avro")
public class AvroController {
    @Autowired
    private EMBSProducer<User> publisher;

    @Value("${spring.kafka.avro.topic}")
    private String topic;

    @Value("${spring.kafka.avro.partition}")
    private Integer partition;

    @Value("${spring.kafka.avro.key}")
    private String key;

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroController.class);

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createVisitor(@RequestBody User user) {

        LOGGER.info("START - /avro/create");
        try {
            publisher.sendMessage(topic, user);
        } catch (Exception e) {

            e.printStackTrace();
            LOGGER.error("Exception :-> " + e);
        }
        LOGGER.info("END - /avro/create");
    }
}
