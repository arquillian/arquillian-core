package org.jboss.weld.deployments.stateful.bothviews;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.LocalI;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LocalCDIInjectionTest extends DeploymentTest
{
   @Inject
   LocalI local;

   @Test
   public void runTest()
   {
      Assert.assertNotNull(local);
      local.ping();
      Assert.assertTrue(local.isPinged());
   }

}
