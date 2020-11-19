package net.securustech.ews.util.functional;

import java.util.function.Consumer;

public class DefaultExceptionConsumer implements Consumer<Exception> {
    @Override
    public void accept(Exception ex) {
        throw new RuntimeException(ex);
    }
}
