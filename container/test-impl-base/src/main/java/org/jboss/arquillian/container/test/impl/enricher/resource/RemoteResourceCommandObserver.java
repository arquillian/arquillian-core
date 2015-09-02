package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.util.Collection;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class RemoteResourceCommandObserver {

   public void lookup(@Observes RemoteResourceCommand command, ServiceLoader serviceLoader) {
      Collection<ResourceProvider> resourceProviders = serviceLoader.all(ResourceProvider.class);
      Class<?> type = command.getType();
      for(ResourceProvider resourceProvider: resourceProviders) {
         if(resourceProvider.canProvide(type)) {
            Object value = resourceProvider.lookup(command.getResource(), command.getAnnotations());
            if(value == null) {
               throw new RuntimeException("Provider for type " + type + " returned a null value: " + resourceProvider);
            }
            command.setResult(value);
            return;
         }
      }
      throw new IllegalArgumentException("No ResourceProvider found for type: " + type);
   }
}
