package org.jboss.weld.deployments.singleton.bothviews;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.LocalI;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class LocalEEInjectionTest extends DeploymentTest
{
   @EJB
   LocalI local;
      
   @Test
   public void runTest()
   {
      Assert.assertNotNull(local);
      local.ping();
      Assert.assertTrue(local.isPinged());
   }
   
}
