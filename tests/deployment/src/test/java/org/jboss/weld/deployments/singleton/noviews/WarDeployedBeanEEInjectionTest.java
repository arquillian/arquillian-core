package org.jboss.weld.deployments.singleton.noviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.singleton.NoViewSingletonEJB;

public class WarDeployedBeanEEInjectionTest extends BeanEEInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(BeanEEInjectionTest.class, NoViewSingletonEJB.class);
   }

}
