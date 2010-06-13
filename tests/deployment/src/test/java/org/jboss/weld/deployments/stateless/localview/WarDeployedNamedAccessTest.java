package org.jboss.weld.deployments.stateless.localview;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.LocalI;
import org.jboss.weld.deployments.beans.stateless.LocalViewStatelessEJB;

public class WarDeployedNamedAccessTest extends NamedAccessTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(NamedAccessTest.class, LocalViewStatelessEJB.class, LocalI.class);
   }

}
