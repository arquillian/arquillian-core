package org.jboss.weld.deployments.stateful.bothviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.LocalI;
import org.jboss.weld.deployments.beans.RemoteI;
import org.jboss.weld.deployments.beans.stateful.BothViewsStatefulEJB;

public class WarDeployedLocalCDIInjectionTest extends LocalCDIInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(LocalCDIInjectionTest.class, BothViewsStatefulEJB.class, RemoteI.class, LocalI.class);
   }

}
