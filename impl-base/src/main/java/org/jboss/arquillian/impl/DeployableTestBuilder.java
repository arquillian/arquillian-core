package org.jboss.arquillian.impl;

import javax.security.auth.login.Configuration;

import org.jboss.arquillian.api.Controlable;
import org.jboss.arquillian.api.Deployer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.tmpdpl.api.container.DeploymentException;

public class DeployableTestBuilder
{
   private DeployableTestBuilder() {}
   
   // TODO: lookup/load container, setup DeployableTest
   public static DeployableTest build(Configuration config) 
   {
      Controlable controller = null;
      Deployer deployer = null;
      
      if(DeployableTest.isInContainer()) 
      {
         controller = new MockContainer();
         deployer = new MockContainer();
      }
      else 
      {
         JbossEmbeddedContainer container = new JbossEmbeddedContainer();
         controller = container;
         deployer = container;
      }

      return new DeployableTest(
            controller,
            deployer
            );
   }
   
   private static class MockContainer implements Controlable, Deployer 
   {
      @Override
      public void start() throws Exception
      {
      }

      @Override
      public void stop() throws Exception
      {
      }

      @Override
      public void deploy(Archive<?> archive) throws DeploymentException
      {
      }

      @Override
      public void undeploy(Archive<?> archive) throws DeploymentException
      {
      }
   }
}
