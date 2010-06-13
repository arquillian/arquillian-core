package org.jboss.weld.deployments.singleton.noviews;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

public class EarDeployedBeanCDIInjectionTest extends BeanCDIInjectionTest
{

   @Deployment
   public static EnterpriseArchive assemble()
   {
      return createEnterpriseArchive(WarDeployedBeanCDIInjectionTest.assemble().addClass(EarDeployedBeanCDIInjectionTest.class));
   }

}
