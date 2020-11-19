package net.securustech.embs;

import net.securustech.embs.receive.KafkaConsumerConfig;
import net.securustech.embs.receive.UsernameHolder;
import net.securustech.embs.util.EMBSHeaderConverter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({KafkaConsumerConfig.class, EMBSHeaderConverter.class, UsernameHolder.class})
public @interface EnableEmbsConsumer {
}
