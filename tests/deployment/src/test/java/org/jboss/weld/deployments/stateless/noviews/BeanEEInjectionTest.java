package org.jboss.weld.deployments.stateless.noviews;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.stateless.NoViewStatelessEJB;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanEEInjectionTest extends DeploymentTest
{
    @EJB
   NoViewStatelessEJB ejb;
   
   @Test
   public void runTest()
   {
      Assert.assertNotNull(ejb);
      ejb.ping();
      Assert.assertTrue(ejb.isPinged());
   }
   
}
