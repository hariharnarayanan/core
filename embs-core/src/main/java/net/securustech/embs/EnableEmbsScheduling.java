package net.securustech.embs;

import net.securustech.embs.zookeeper.lock.EmbsSchedulingConfig;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableScheduling
@EnableEmbsLock
@Import({EmbsSchedulingConfig.class})
public @interface EnableEmbsScheduling {
}
