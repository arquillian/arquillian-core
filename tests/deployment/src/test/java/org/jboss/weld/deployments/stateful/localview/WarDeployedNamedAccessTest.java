package org.jboss.weld.deployments.stateful.localview;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.LocalI;
import org.jboss.weld.deployments.beans.stateful.LocalViewStatefulEJB;

public class WarDeployedNamedAccessTest extends NamedAccessTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(NamedAccessTest.class, LocalViewStatefulEJB.class, LocalI.class);
   }

}
