package org.jboss.weld.deployments.war.stateful;

import javax.ejb.EJB;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.stateful.NoViewStatefulEJB;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class NoViewsAccessTest extends DeploymentTest
{
   @Inject
   NoViewStatefulEJB ejb;

   @EJB
   NoViewStatefulEJB ejb_;
   
   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(NoViewStatefulEJB.class);
   }
   
   @Test
   public void runTest()
   {
      Assert.assertNotNull(ejb);
      Assert.assertNotNull(ejb_);
      ejb.ping();
      ejb_.ping();
      Assert.assertTrue(ejb.isPinged());
      Assert.assertTrue(ejb_.isPinged());
      //Assert.assertTrue(getNamedBean("noViewStatefulEJB", NoViewStatefulEJB.class).isPinged());
   }
   
   
}
