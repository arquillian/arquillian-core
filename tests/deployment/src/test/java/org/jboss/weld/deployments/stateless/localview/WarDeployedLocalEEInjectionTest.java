package org.jboss.weld.deployments.stateless.localview;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.LocalI;
import org.jboss.weld.deployments.beans.stateless.LocalViewStatelessEJB;

public class WarDeployedLocalEEInjectionTest extends LocalEEInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(LocalEEInjectionTest.class, LocalViewStatelessEJB.class, LocalI.class);
   }

}
