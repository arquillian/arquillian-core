package org.jboss.weld.deployments.stateful.noviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

public class EarDeployedNamedAccessTest extends NamedAccessTest
{

   @Deployment
   public static EnterpriseArchive assemble()
   {
      return createEnterpriseArchive(WarDeployedNamedAccessTest.assemble().addClass(EarDeployedNamedAccessTest.class));
   }

}
