package org.jboss.weld.deployments.stateful.noviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.stateful.NoViewStatefulEJB;

public class WarDeployedBeanCDIInjectionTest extends BeanCDIInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(BeanCDIInjectionTest.class, NoViewStatefulEJB.class);
   }

}
