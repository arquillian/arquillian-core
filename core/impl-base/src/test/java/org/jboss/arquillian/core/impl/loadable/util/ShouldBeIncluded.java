package org.jboss.arquillian.core.impl.loadable.util;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;

public class ShouldBeIncluded implements FakeService
{
   @Inject
   private Instance<String> injectionPoint;
   
   @Override
   public boolean isValid()
   {
      return injectionPoint != null;
   }
}
