package org.jboss.weld.deployments.stateful.remoteview;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.RemoteI;
import org.jboss.weld.deployments.beans.stateful.RemoteViewStatefulEJB;

public class WarDeployedRemoteEEInjectionTest extends RemoteEEInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(RemoteEEInjectionTest.class, RemoteViewStatefulEJB.class, RemoteI.class);
   }

}
