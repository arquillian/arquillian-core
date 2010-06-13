package org.jboss.weld.deployments.stateless.bothviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.RemoteI;
import org.jboss.weld.deployments.beans.stateless.RemoteViewStatelessEJB;

public class WarDeployedNamedAccessTest extends NamedAccessTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(NamedAccessTest.class, RemoteViewStatelessEJB.class, RemoteI.class);
   }

}
