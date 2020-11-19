package net.securustech.embs;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class KafkaExecutionTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaExecutionTimer.class);

    public KafkaExecutionTimer() {

    }

    @Pointcut("execution(* net.securustech.embs.send..*Producer.*(..))")
    public void methodPointcut() {}

    @Around("methodPointcut()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {

        StopWatch sw = new StopWatch();
        String name = pjp.getSignature().getName();
        try {
            sw.start();
            return pjp.proceed();
        } finally {
            sw.stop();
            LOGGER.info("STOPWATCH :-> " + sw.getTotalTimeSeconds() + " - " + name);
        }
    }
}