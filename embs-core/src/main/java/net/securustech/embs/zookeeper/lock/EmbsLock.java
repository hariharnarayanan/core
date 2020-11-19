package net.securustech.embs.zookeeper.lock;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EmbsLock {
    String name() default "";
}
