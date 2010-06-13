package org.jboss.weld.deployments.stateful.noviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

public class EarDeployedBeanEEInjectionTest extends BeanEEInjectionTest
{

   @Deployment
   public static EnterpriseArchive assemble()
   {
      return createEnterpriseArchive(WarDeployedBeanEEInjectionTest.assemble().addClass(EarDeployedBeanEEInjectionTest.class));
   }

}
