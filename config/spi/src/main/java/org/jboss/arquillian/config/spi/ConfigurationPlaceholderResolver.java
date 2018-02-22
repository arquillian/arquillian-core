package org.jboss.arquillian.config.spi;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;

public interface ConfigurationPlaceholderResolver {

    ArquillianDescriptor resolve(ArquillianDescriptor arquillianDescriptor);

    /**
     * In case of registering more than one placeholder resolver, they are ordered as they appear on classpath.
     * If you need to reorder them, you can use {@code precedence} value. The higher the {@code precedence} is,
     * the sooner the observer is executed.
     * @return
     */
    int precedence();
}
