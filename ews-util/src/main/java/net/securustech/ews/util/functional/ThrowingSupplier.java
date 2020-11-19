package net.securustech.ews.util.functional;

import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;

    static <T> Supplier<T> throwingSupplierWrapper(ThrowingSupplier<T, Exception> throwingSupplier, Consumer<Exception> exceptionConsumer) {
        return () -> {
            try {
                return throwingSupplier.get();
            } catch(Exception ex) {
                exceptionConsumer.accept(ex);
                return null;
            }
        };
    }

    static <T> Supplier<T> throwingSupplierWrapper(ThrowingSupplier<T, Exception> throwingSupplier) {
        return throwingSupplierWrapper(throwingSupplier, new DefaultExceptionConsumer());
    }
}
