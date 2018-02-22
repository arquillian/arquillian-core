package org.jboss.arquillian.config.impl.extension;

import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.spi.ConfigurationPlaceholderResolver;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.impl.context.SuiteContextImpl;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.mockito.Mockito;

public class AbstractReplacementInArqXmlTestBase extends AbstractManagerTestBase {

    /**
     * The loaded arquillian.xml
     */
    @Inject
    protected Instance<ArquillianDescriptor> desc;

    @Override
    protected void beforeStartManager(Manager manager) {

        final ServiceLoader serviceLoader = Mockito.mock(ServiceLoader.class);
        startContexts(manager);
        final ConfigurationPlaceholderResolver configurationSysPropResolver = new SystemPropertiesConfigurationPlaceholderResolver();
        final ConfigurationPlaceholderResolver classpathConfigurationPlaceholderResolver = new ClasspathConfigurationPlaceholderResolver();

        Mockito.when(serviceLoader.all(ConfigurationPlaceholderResolver.class))
            .thenReturn(Arrays.asList(classpathConfigurationPlaceholderResolver, configurationSysPropResolver));

        bind(SuiteScoped.class, ServiceLoader.class, serviceLoader);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.core.test.AbstractManagerTestBase#addExtensions(java.util.List)
     */
    @Override
    protected void addExtensions(final List<Class<?>> extensions) {
        extensions.add(ConfigurationRegistrar.class);
        super.addExtensions(extensions);
    }

    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(SuiteContextImpl.class);
    }

    @Override
    protected void startContexts(Manager manager) {
        super.startContexts(manager);
        manager.getContext(SuiteContext.class).activate();
    }


}
