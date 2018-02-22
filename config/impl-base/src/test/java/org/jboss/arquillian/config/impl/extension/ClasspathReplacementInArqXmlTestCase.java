package org.jboss.arquillian.config.impl.extension;

import java.net.URL;
import org.jboss.arquillian.config.descriptor.impl.AssertXPath;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.jboss.arquillian.config.descriptor.impl.AssertXPath.assertXPath;

public class ClasspathReplacementInArqXmlTestCase extends AbstractReplacementInArqXmlTestBase {

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

        // when
        final String xml = desc.get().exportAsString();

        // then
        AssertXPath.assertXPath(xml, "/arquillian/container/@qualifier", VALUE_EL_OVERRIDE);
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("arquillian_sysprop.xml");
        assertXPath(xml, "/arquillian/container/configuration/property",
            String.format("-Djavax.net.ssl.trustStore=%s", resource.toString()));
    }

}
