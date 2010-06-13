package org.jboss.weld.deployments.stateful.remoteview;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.RemoteI;
import org.jboss.weld.deployments.beans.stateful.RemoteViewStatefulEJB;

public class WarDeployedNamedAccessTest extends NamedAccessTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(NamedAccessTest.class, RemoteViewStatefulEJB.class, RemoteI.class);
   }

}
