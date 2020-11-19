package net.securustech.embs;

import net.securustech.embs.send.EMBSProducerImpl;
import net.securustech.embs.send.KafkaProducerConfig;
import net.securustech.embs.send.KafkaService;
import net.securustech.embs.util.EMBSHeaderConverter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({KafkaProducerConfig.class, EMBSHeaderConverter.class, EMBSProducerImpl.class, KafkaService.class})
public @interface EnableEmbsProducer {
}
