package org.jboss.arquillian.junit5.extension;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.junit5.EnclosingInstancesEnricher;
import org.jboss.arquillian.junit5.MethodParameterObserver;

/**
 * The client side extension for JUnit Jupiter tests.
 *
 * @author Zoltan Forgo
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Radoslav Husar
 */
public class JUnitJupiterCoreExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(MethodParameterObserver.class)
                .observer(EnclosingInstancesEnricher.class);
    }
}
