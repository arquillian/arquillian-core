package org.jboss.arquillian.junit5;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.jupiter.api.extension.ExtensionContext;

class SuiteFinalizer implements ExtensionContext.Store.CloseableResource {
    
    private final AdaptorManager manager;
    private final TestRunnerAdaptor adaptor;

    public SuiteFinalizer(AdaptorManager manager, TestRunnerAdaptor adaptor) {
        this.manager = manager;
        this.adaptor = adaptor;
    }

    @Override
    public void close() throws Throwable {
        manager.shutdown(adaptor);
    }
}
