package org.jboss.weld.deployments.singleton.localview;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.LocalI;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class LocalCDIInjectionTest extends DeploymentTest
{
   @Inject
   LocalI ejb;
   
   @Test
   public void runTest()
   {
      Assert.assertNotNull(ejb);
      ejb.ping();
      Assert.assertTrue(ejb.isPinged());
   }
   
}
