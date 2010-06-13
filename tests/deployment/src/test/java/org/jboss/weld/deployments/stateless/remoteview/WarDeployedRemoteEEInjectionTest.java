package org.jboss.weld.deployments.stateless.remoteview;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.RemoteI;
import org.jboss.weld.deployments.beans.stateless.RemoteViewStatelessEJB;

public class WarDeployedRemoteEEInjectionTest extends RemoteEEInjectionTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(RemoteEEInjectionTest.class, RemoteViewStatelessEJB.class, RemoteI.class);
   }

}
