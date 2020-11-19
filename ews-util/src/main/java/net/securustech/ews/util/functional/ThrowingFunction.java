package net.securustech.ews.util.functional;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;

    static <T, R> Function<T, R> throwingFunctionWrapper(ThrowingFunction<T, R, Exception> throwingFunction, Consumer<Exception> exceptionConsumer) {
        return i -> {
            try {
                return throwingFunction.apply(i);
            } catch(Exception ex) {
                exceptionConsumer.accept(ex);
                return null;
            }
        };
    }

    static <T, R> Function<T, R> throwingFunctionWrapper(ThrowingFunction<T, R, Exception> throwingFunction) {
        return throwingFunctionWrapper(throwingFunction, new DefaultExceptionConsumer());
    }
}
