package org.jboss.weld.deployments.singleton.bothviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

public class EarDeployedLocalEEInjectionTest extends LocalEEInjectionTest
{

   @Deployment
   public static EnterpriseArchive assemble()
   {
      return createEnterpriseArchive(WarDeployedLocalEEInjectionTest.assemble().addClass(EarDeployedLocalEEInjectionTest.class));
   }

}
