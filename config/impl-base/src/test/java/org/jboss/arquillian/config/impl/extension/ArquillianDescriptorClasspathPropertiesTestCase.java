package org.jboss.arquillian.config.impl.extension;

import java.net.URL;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Test;

import static org.jboss.arquillian.config.descriptor.impl.AssertXPath.assertXPath;

public class ArquillianDescriptorClasspathPropertiesTestCase {

    @Test
    public void should_replace_properties_with_classpath_location() throws Exception {
        // given
        String element = "arquillian_sysprop.xml";
        ArquillianDescriptor desc = create()
            .container("daemon")
            .property("javaVmArguments", "-Djavax.net.ssl.trustStore=${classpath(" + element + ")}");
        final ClasspathConfigurationPlaceholderResolver classpathConfigurationPlaceholderResolver = new ClasspathConfigurationPlaceholderResolver();

        // when
        desc = classpathConfigurationPlaceholderResolver.resolve(desc);

        // then
        final String descString = desc.exportAsString();
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(element);
        assertXPath(descString, "/arquillian/container/configuration/property",
            String.format("-Djavax.net.ssl.trustStore=%s", resource.toString()));

    }

    @Test
    public void should_not_replace_placeholder_if_classpath_resource_not_found() throws Exception {
        String element = "notfound.txt";
        ArquillianDescriptor desc = create()
            .container("daemon")
            .property("javaVmArguments", "-Djavax.net.ssl.trustStore=${classpath(" + element + ")}");
        final ClasspathConfigurationPlaceholderResolver classpathConfigurationPlaceholderResolver = new ClasspathConfigurationPlaceholderResolver();

        // when
        desc = classpathConfigurationPlaceholderResolver.resolve(desc);

        // then
        final String descString = desc.exportAsString();
        assertXPath(descString, "/arquillian/container/configuration/property",
            String.format("-Djavax.net.ssl.trustStore=${classpath(%s)}", element));
    }

    @Test
    public void should_not_replace_other_placeholders() throws Exception {

        ArquillianDescriptor desc = create()
            .container("daemon")
            .property("javaVmArguments", "-Djavax.net.ssl.trustStore=${env.LAUNCHER_TESTS_TRUSTSTORE_PATH}");
        final ClasspathConfigurationPlaceholderResolver classpathConfigurationPlaceholderResolver = new ClasspathConfigurationPlaceholderResolver();

        // when
        desc = classpathConfigurationPlaceholderResolver.resolve(desc);

        // then
        final String descString = desc.exportAsString();
        assertXPath(descString, "/arquillian/container/configuration/property",
            "-Djavax.net.ssl.trustStore=${env.LAUNCHER_TESTS_TRUSTSTORE_PATH}");

    }


    private ArquillianDescriptor create() {
        return Descriptors.create(ArquillianDescriptor.class);
    }

}
