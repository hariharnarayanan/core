package net.securustech.embs.producer;

import net.securustech.embs.send.EMBSProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/publish")
public class EMBSRestController {

    @Autowired
    private EMBSProducer publisher;

    private static final Logger LOGGER = LoggerFactory.getLogger(EMBSRestController.class);

    @RequestMapping(value = "/{topic}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publishMessage(@PathVariable String topic, @RequestBody String embsMessage){

        LOGGER.info("START - /publish Message on TOPIC :-> " + topic + " :-> Payload : " + embsMessage);
        try {

            HashMap<String, Object> rawHeaders = new HashMap<>();
            rawHeaders.put("RETRY_COUNT", Integer.valueOf(2));
            rawHeaders.put("ACTION", "BO");

            publisher.sendMessageWithHeader(topic, 0, "embs", embsMessage, rawHeaders);

        } catch (Exception e) {

            e.printStackTrace();
            LOGGER.error("Exception :-> " + e);
        }
        LOGGER.info("END - /publish Message on TOPIC :-> " + topic + " :-> Payload : " + embsMessage);

    }
}
