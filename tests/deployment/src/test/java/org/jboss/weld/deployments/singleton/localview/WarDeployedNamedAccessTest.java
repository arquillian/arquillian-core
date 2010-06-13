package org.jboss.weld.deployments.singleton.localview;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.beans.LocalI;
import org.jboss.weld.deployments.beans.RemoteI;
import org.jboss.weld.deployments.beans.singleton.LocalViewSingletonEJB;

public class WarDeployedNamedAccessTest extends NamedAccessTest
{

   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(NamedAccessTest.class, LocalViewSingletonEJB.class, LocalI.class);
   }

}
