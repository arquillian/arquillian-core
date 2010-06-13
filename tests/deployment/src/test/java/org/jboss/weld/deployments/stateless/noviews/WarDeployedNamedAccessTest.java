package org.jboss.weld.deployments.stateless.noviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.RemoteI;
import org.jboss.weld.deployments.beans.stateless.NoViewStatelessEJB;

public class WarDeployedNamedAccessTest extends NamedAccessTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(NamedAccessTest.class, NoViewStatelessEJB.class);
   }

}
