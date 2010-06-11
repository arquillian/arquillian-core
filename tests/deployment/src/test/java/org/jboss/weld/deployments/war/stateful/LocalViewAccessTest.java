package org.jboss.weld.deployments.war.stateful;

import javax.ejb.EJB;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.deployments.DeploymentTest;
import org.jboss.weld.deployments.beans.LocalI;
import org.jboss.weld.deployments.beans.stateful.LocalViewStatefulEJB;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LocalViewAccessTest extends DeploymentTest
{
   @Inject
   LocalI ejb;

   @EJB
   LocalI local;
   
   @Inject 
   BeanManager beanManager;
   
   @Deployment
   public static JavaArchive assemble()
   {
      return createCDIArchive(LocalViewStatefulEJB.class, LocalI.class);
   }
   
   @Test
   public void runTest()
   {
      Assert.assertNotNull(ejb);
      Assert.assertNotNull(local);
      ejb.ping();
      local.ping();
      Assert.assertTrue(ejb.isPinged());
      Assert.assertTrue(local.isPinged());
      //Assert.assertTrue(getNamedBean("localViewStatefulEJB", LocalViewStatefulEJB.class).isPinged());
   }
   
   
}
