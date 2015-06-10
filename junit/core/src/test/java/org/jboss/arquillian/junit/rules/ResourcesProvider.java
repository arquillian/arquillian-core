package org.jboss.arquillian.junit.rules;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * Provides to test class an instance of the class {@link ResourcesImpl}.
 * 
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class ResourcesProvider implements ResourceProvider
{

    @Override
    public boolean canProvide(Class<?> type)
    {
        return type.equals(ResourcesImpl.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers)
    {
        return new ResourcesImpl();
    }
}
