package org.jboss.weld.deployments.stateless.noviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.stateless.NoViewStatelessEJB;

public class WarDeployedBeanCDIInjectionTest extends BeanCDIInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(BeanCDIInjectionTest.class, NoViewStatelessEJB.class);
   }

}
