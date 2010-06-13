package org.jboss.weld.deployments.singleton.bothviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

public class EarDeployedRemoteEEInjectionTest extends RemoteEEInjectionTest
{

   @Deployment
   public static EnterpriseArchive assemble()
   {
      return createEnterpriseArchive(WarDeployedRemoteEEInjectionTest.assemble().addClass(EarDeployedRemoteEEInjectionTest.class));
   }

}
