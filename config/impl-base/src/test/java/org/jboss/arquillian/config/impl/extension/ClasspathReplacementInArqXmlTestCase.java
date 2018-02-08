package org.jboss.arquillian.config.impl.extension;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.impl.AssertXPath;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.jboss.arquillian.config.descriptor.impl.AssertXPath.assertXPath;

@RunWith(MockitoJUnitRunner.class)
public class ClasspathReplacementInArqXmlTestCase extends AbstractManagerTestBase {

    @Mock
    private ServiceLoader serviceLoader;

    /**
     * Name of the arquillian.xml to test
     */
    private static final String NAME_ARQ_XML = "arquillian_classpathprop.xml";

    /**
     * Name of the system property for EL expressions
     */
    private static final String SYSPROP_ARQ_CONTAINER = "arquillian.container";

    private static final String VALUE_EL_OVERRIDE = "ALR";

    /**
     * The loaded arquillian.xml
     */
    @Inject
    private Instance<ArquillianDescriptor> desc;

    /**
     * Sets the name of the arquillian.xml under test
     */
    @BeforeClass
    public static void setSysprops() {
        // Set a sysprop to denote the name of the arquillian.xml under test
        System.setProperty(ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY, NAME_ARQ_XML);

        // Set a sysprop to override the name of the qualifier
        System.setProperty(SYSPROP_ARQ_CONTAINER, VALUE_EL_OVERRIDE);
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void clearSysprops() {
        System.clearProperty(ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY);
        System.clearProperty(SYSPROP_ARQ_CONTAINER);
    }

    @Test
    public void should_replace_classpath_in_arquillian_xml() throws Exception {

        final String xml = desc.get().exportAsString();
        System.out.println(xml);
        AssertXPath.assertXPath(xml, "/arquillian/container/@qualifier", VALUE_EL_OVERRIDE);
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("arquillian_sysprop.xml");
        assertXPath(xml, "/arquillian/container/configuration/property",
            String.format("-Djavax.net.ssl.trustStore=%s", resource.toString()));
    }

    @Override
    protected void beforeStartManager(Manager manager) {
        startContexts(manager);
        final ConfigurationPlaceholderResolver configurationSysPropResolver = new ConfigurationSysPropResolver();
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
