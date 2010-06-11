package org.jboss.weld.deployments.war.visibility;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.stateful.NoViewStatefulEJB;
import org.jboss.weld.deployments.beans.stateful.NoViewStatefulEJB2;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WarTwoJarPackedVisibilityTest extends DeploymentTest
{
   @Inject
   NoViewStatefulEJB ejb;
   
   @Inject
   NoViewStatefulEJB2 ejb_;
   
   @Deployment
   public static WebArchive assemble()
   {
      return createWebArchive(createCDIArchive("one.jar", NoViewStatefulEJB.class), createCDIArchive("two.jar", NoViewStatefulEJB2.class));
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
   }

}
