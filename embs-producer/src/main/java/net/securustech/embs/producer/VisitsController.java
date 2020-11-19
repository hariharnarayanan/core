package net.securustech.embs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.embs.send.EMBSProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visits")
public class VisitsController {

    @Autowired
    private EMBSProducer publisher;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.kafka.visits.topic}")
    private String topic;

    @Value("${spring.kafka.visits.partition}")
    private Integer partition;

    @Value("${spring.kafka.visits.key}")
    private String key;

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitsController.class);

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createVisit(@RequestBody KafkaEvent kafkaEvent){

        LOGGER.info("START - /visits/create");
        try {

            publisher.sendMessage(topic, objectMapper.writeValueAsString(kafkaEvent));

        } catch (Exception e) {

            e.printStackTrace();
            LOGGER.error("Exception :-> " + e);
        }
        LOGGER.info("END - /visits/create");

    }
}
