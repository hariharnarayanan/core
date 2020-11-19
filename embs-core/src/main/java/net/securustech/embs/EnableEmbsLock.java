package net.securustech.embs;

import net.securustech.embs.zookeeper.lock.EmbsLockAspect;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({EmbsLockAspect.class})
public @interface EnableEmbsLock {
}
