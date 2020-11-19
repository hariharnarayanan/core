package net.securustech.embs.receive;

import net.securustech.embs.EmbsConstants;
import net.securustech.embs.util.ClientIdentifier;
import net.securustech.embs.util.EMBSHeaderConverter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Aspect
@Component
@Order(0)
public class UsernameHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsernameHolder.class);

    private static ThreadLocal<String> accessUser = new ThreadLocal<>();

    public static String getUsername() {
        return accessUser.get();
    }

    @Autowired
    private ClientIdentifier clientIdentifier;

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener) && args(consumerRecord,..)")
    public Object retrieveKafkaHeader(ProceedingJoinPoint pjp, ConsumerRecord consumerRecord) throws  Throwable {
        String userName = Optional.ofNullable(consumerRecord.headers().lastHeader(EmbsConstants.HEADER_KEY_USERNAME)).map(header -> {
            try {
                return (String)EMBSHeaderConverter.deserialize(header.value());
            } catch (Exception ex) {
                return clientIdentifier.getApplicationName().toUpperCase();
            }
        }).orElse(clientIdentifier.getApplicationName().toUpperCase());

        accessUser.set(userName);

        try {
            return pjp.proceed();
        } finally {
            accessUser.remove();
        }
    }
}
