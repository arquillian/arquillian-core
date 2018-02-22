package org.jboss.arquillian.config.impl.extension;

import java.net.URL;
import java.util.logging.Logger;

class ClasspathPropertyResolver implements PropertyResolver {

    private final static Logger logger = Logger.getLogger(ClasspathConfigurationPlaceholderResolver.class.getName());

    /**
     * Classpath base property
     */
    private static final String CLASSPATH = "classpath(";

    public String getValue(String key) {

        if (key.startsWith(CLASSPATH)) {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            String classpathResource = key.substring(CLASSPATH.length(), key.length() - 1);
            final URL resource = contextClassLoader.getResource(classpathResource);

            //If resource is not found it is returned as null so no change is applicable.
            if (resource == null) {
                logger.warning(String.format("Resource %s is not found on the classspath so the property %s is not replaced.", classpathResource, key));
                return null;
            }

            return resource.toString();
        }

        return null;
    }
}
