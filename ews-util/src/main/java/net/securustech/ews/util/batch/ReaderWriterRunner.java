package net.securustech.ews.util.batch;

import net.securustech.ews.util.functional.ThrowingConsumer;
import net.securustech.ews.util.functional.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Configuration
public class ReaderWriterRunner {
    @Autowired
    @Qualifier("writerThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor writerThreadPoolTaskExecutor;

    @Autowired
    @Qualifier("readerThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor readerThreadPoolTaskExecutor;

    public <T> void submit(
        ThrowingSupplier<T, Exception> readerFunction,
        ThrowingConsumer<T, Exception> writerFunction,
        Consumer<Exception> exceptionHandler
    ) {
        CompletableFuture
            .supplyAsync(ThrowingSupplier.throwingSupplierWrapper(readerFunction), readerThreadPoolTaskExecutor)
            .thenAcceptAsync(ThrowingConsumer.throwingConsumerWrapper(writerFunction), writerThreadPoolTaskExecutor)
            .exceptionally(ex ->{
                exceptionHandler.accept((Exception)ex);
                return null;
            });
    }
}
