package org.jboss.arquillian.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;

public class AutoCloserObject implements ExtensionContext.Store.CloseableResource {
    
    private final AdaptorManager adaptor;

    public AutoCloserObject(AdaptorManager adaptor) {
        System.out.println("\n\n\n\n Statted all ALL STARTTTTT !!!!!!!!!!!!\n\n\n");
        this.adaptor = adaptor;
    }

    @Override
    public void close() throws Throwable {
        System.out.println("\n\n\n\n After all ALL ME KILLED !!!!!!!!!!!!\n\n\n");
        adaptor.shutdown(adaptor.getAdaptor());
    }
}
