package net.securustech.ews.util.functional;

import java.util.function.Consumer;
import java.util.function.Predicate;

@FunctionalInterface
public interface ThrowingPredicate<T, E extends Exception> {
    boolean test(T t) throws E;

    static <T> Predicate<T> throwingPredicateWrapper(ThrowingPredicate<T, Exception> throwingPredicate, Consumer<Exception> exceptionConsumer) {
        return i -> {
            try {
                return throwingPredicate.test(i);
            } catch(Exception ex) {
                exceptionConsumer.accept(ex);
                return false;
            }
        };
    }

    static <T> Predicate<T> throwingPredicateWrapper(ThrowingPredicate<T, Exception> throwingPredicate) {
        return throwingPredicateWrapper(throwingPredicate, new DefaultExceptionConsumer());
    }
}
