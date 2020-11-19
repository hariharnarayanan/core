package net.securustech.ews.util.functional;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E;

    static <T> Consumer<T> throwingConsumerWrapper(ThrowingConsumer<T, Exception> throwingConsumer, Consumer<Exception> exceptionConsumer) {
        return i -> {
            try {
                throwingConsumer.accept(i);
            } catch(Exception ex) {
                exceptionConsumer.accept(ex);
            }
        };
    }

    static <T> Consumer<T> throwingConsumerWrapper(ThrowingConsumer<T, Exception> throwingConsumer) {
        return throwingConsumerWrapper(throwingConsumer, new DefaultExceptionConsumer());
    }
}
