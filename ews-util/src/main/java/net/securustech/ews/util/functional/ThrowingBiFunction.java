package net.securustech.ews.util.functional;


import java.util.function.BiFunction;
import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingBiFunction<R1, R2, T, E extends Exception> {
    T apply(R1 r1, R2 r2) throws E;

    static <R1, R2, T> BiFunction<R1, R2, T> throwingBiFunctionWrapper(ThrowingBiFunction<R1, R2, T, Exception> throwingBiFunction, Consumer<Exception> exceptionConsumer) {
        return (R1 r1, R2 r2) -> {
            try {
                return throwingBiFunction.apply(r1, r2);
            } catch(Exception ex) {
                exceptionConsumer.accept(ex);
                return null;
            }
        };
    }

    static <R1, R2, T> BiFunction<R1, R2, T> throwingBiFunctionWrapper(ThrowingBiFunction<R1, R2, T, Exception> throwingBiFunction) {
        return throwingBiFunctionWrapper(throwingBiFunction, new DefaultExceptionConsumer());
    }
}
