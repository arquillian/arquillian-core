package org.jboss.weld.deployments.stateful.noviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.stateful.NoViewStatefulEJB;

public class WarDeployedBeanEEInjectionTest extends BeanEEInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(BeanEEInjectionTest.class, NoViewStatefulEJB.class);
   }

}
