package org.jboss.weld.deployments.singleton.remoteview;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.weld.deployments.DeploymentTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class NamedAccessTest extends DeploymentTest
{
   @Inject
   BeanManager beanManager;
   
   @Test
   public void runTest()
   {
      Assert.assertNotNull(getNamedBean("remoteViewSingletonEJB", Object.class));
   }
   
}
