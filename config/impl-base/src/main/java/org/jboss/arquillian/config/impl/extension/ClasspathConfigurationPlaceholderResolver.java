package org.jboss.arquillian.config.impl.extension;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.spi.ConfigurationPlaceholderResolver;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

class ClasspathConfigurationPlaceholderResolver implements ConfigurationPlaceholderResolver {

    public ArquillianDescriptor resolve(ArquillianDescriptor descriptor) {

        final String descriptorAsString = descriptor.exportAsString();
        return Descriptors.importAs(ArquillianDescriptor.class)
            .fromString(StringPropertyReplacer.replaceClasspath(descriptorAsString));

    }

    public int precedence() {
        return 0;
    }
}
