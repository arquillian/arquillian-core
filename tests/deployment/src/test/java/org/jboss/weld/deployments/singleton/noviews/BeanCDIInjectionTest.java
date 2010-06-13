package org.jboss.weld.deployments.singleton.noviews;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.singleton.NoViewSingletonEJB;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class BeanCDIInjectionTest extends DeploymentTest
{
   @Inject
   NoViewSingletonEJB ejb;

   @Test
   public void runTest()
   {
      Assert.assertNotNull(ejb);
      ejb.ping();
      Assert.assertTrue(ejb.isPinged());
   }

}
