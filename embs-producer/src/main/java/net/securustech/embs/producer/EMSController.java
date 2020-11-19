package net.securustech.embs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.embs.send.EMBSProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/ems")
public class EMSController {

    @Autowired
    private EMBSProducer publisher;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.kafka.ems.topic}")
    private String topic;

    @Value("${spring.kafka.ems.partition}")
    private Integer partition;

    @Value("${spring.kafka.ems.key}")
    private String key;

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitsController.class);

    @RequestMapping(value = "/send", method = RequestMethod.POST, headers="Content-Type=application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publishMessage(@RequestBody KafkaEvent kafkaEvent){

        LOGGER.info("START - /ems/send");
        Response response = null;
        try {

            publisher.sendMessage(topic, objectMapper.writeValueAsString(kafkaEvent));

        } catch (Exception e) {

            e.printStackTrace();
            LOGGER.error("Exception :-> " + e);
        }
        LOGGER.info("END - /ems/send \nResponse :-> " + response);

    }
}
