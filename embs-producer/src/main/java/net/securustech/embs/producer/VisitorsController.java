/**
 * Created by Himanshu on 9/6/16.
 */
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
@RequestMapping("/visitors")
public class VisitorsController {

    @Autowired
    private EMBSProducer publisher;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.kafka.visitors.topic}")
    private String topic;

    @Value("${spring.kafka.visitors.partition}")
    private Integer partition;

    @Value("${spring.kafka.visitors.key}")
    private String key;

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorsController.class);

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createVisitor(@RequestBody Visitor visitor) {

        LOGGER.info("START - /visitors/create");
        try {

            publisher.sendMessage(topic, objectMapper.writeValueAsString(visitor));
//            publisher.sendMessage(topic, new User("test", 1, "red"));
//            response = publisher.sendMessage(topic, partition, key, objectMapper.writeValueAsString(visitor));
//            response = publisher.sendSyncMessage(topic, partition, key, objectMapper.writeValueAsString(visitor));

        } catch (Exception e) {

            e.printStackTrace();
            LOGGER.error("Exception :-> " + e);
        }
        LOGGER.info("END - /visitors/create");
    }
}
