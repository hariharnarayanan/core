package net.securustech.ews.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ESCommonConfig {

    private static final Logger log = LoggerFactory.getLogger(ESCommonConfig.class);


    @Value("${ews.internal.delay}")
    String ewsInternalDelay;

    @Value("${ews.internal.multiplier}")
    String ewsInternalMultiplier;

    @Value("${ews.internal.max.attempts}")
    String ewsInternalMaxAttempts;

    @Value("${ews.internal.max.delay}")
    String ewsInternalMaxDelay;


    @Bean
    public String ewsInternalDelay() {
        log.info(ewsInternalDelay);
        return ewsInternalDelay;
    }

    @Bean
    public String ewsInternalMultiplier() {
        log.info(ewsInternalMultiplier);
        return ewsInternalMultiplier;
    }

    @Bean
    public String ewsInternalMaxAttempts() {
        log.info(ewsInternalMaxAttempts);
        return ewsInternalMaxAttempts;
    }

    @Bean
    public String ewsInternalMaxDelay() {
        log.info(ewsInternalMaxDelay);
        return ewsInternalMaxDelay;
    }







}
