package net.securustech.ews;

import net.securustech.ews.exception.entities.EWSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    private final static int THREAD_NUMBER = Runtime.getRuntime().availableProcessors();

    @Value("${esp.initialLoad.readerThreadNumber: 4}")
    private int readerThreadNumber;

    @Value("${esp.initialLoad.writerThreadNumber: 4}")
    private int writerThreadNumber;


    @Bean(name = "readerThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor readerThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(readerThreadNumber);
        executor.setMaxPoolSize(readerThreadNumber);
        executor.setThreadNamePrefix("reader_thread");
        executor.initialize();
        return executor;
    }

    @Bean(name = "writerThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor writerhreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(writerThreadNumber);
        executor.setMaxPoolSize(writerThreadNumber);
        executor.setThreadNamePrefix("writer_thread");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}

class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable e, Method method, Object... objects) {
        if (e instanceof EWSException) {
            LOGGER.error("EWSException <<::>> EWSErrorCode :-> " + ((EWSException)e).getEwsErrorCode() + " : EWSErrorMessage :-> " + ((EWSException)e).getEwsErrorMessage(), e);

            LOGGER.error("EWSException <<::>> DownstreamErrorCode :-> " + ((EWSException) e).getDownstreamErrorCode() + " : DownstreamErrorMessage :-> " + ((EWSException) e).getDownstreamErrorMessage(), e);
        }
        else {
            LOGGER.error("Throwable <<::>> ErrorMessage :-> " + e.getMessage(), e);
        }
    }
}
