package org.jboss.arquillian.junit5.extension;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.junit5.MethodParameterObserver;

public class JUnitJupiterCoreExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(MethodParameterObserver.class);
    }
}
