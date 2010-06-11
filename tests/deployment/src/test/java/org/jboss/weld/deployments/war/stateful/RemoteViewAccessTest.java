package org.jboss.weld.deployments.war.stateful;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.RemoteI;
import org.jboss.weld.deployments.beans.stateful.RemoteViewStatefulEJB;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class RemoteViewAccessTest extends DeploymentTest
{
   @EJB
   RemoteI ejb;
   
   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(RemoteViewStatefulEJB.class, RemoteI.class);
   }
   
   @Test
   public void runTest()
   {
      Assert.assertNotNull(ejb);
      ejb.ping();
      Assert.assertTrue(ejb.isPinged());
      //Assert.assertTrue(getNamedBean("remoteViewStatefulEJB", RemoteViewStatefulEJB.class).isPinged());
   }
   
   
}
