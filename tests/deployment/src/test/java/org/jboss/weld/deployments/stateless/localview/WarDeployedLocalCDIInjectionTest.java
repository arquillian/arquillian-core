package org.jboss.weld.deployments.stateless.localview;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.LocalI;
import org.jboss.weld.deployments.beans.stateless.LocalViewStatelessEJB;

public class WarDeployedLocalCDIInjectionTest extends LocalCDIInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(LocalCDIInjectionTest.class, LocalViewStatelessEJB.class, LocalI.class);
   }

}
