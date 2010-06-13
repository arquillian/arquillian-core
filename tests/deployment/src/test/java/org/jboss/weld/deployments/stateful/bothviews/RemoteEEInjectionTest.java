package org.jboss.weld.deployments.stateful.bothviews;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.RemoteI;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class RemoteEEInjectionTest extends DeploymentTest
{
   @EJB
   RemoteI remote;
     
   @Test
   public void runTest()
   {
      Assert.assertNotNull(remote);
      remote.ping();
      Assert.assertTrue(remote.isPinged());
   }
   
}
