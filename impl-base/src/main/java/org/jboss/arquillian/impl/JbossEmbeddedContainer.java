package org.jboss.arquillian.impl;

import org.jboss.arquillian.api.Controlable;
import org.jboss.arquillian.api.Deployer;
import org.jboss.embedded.api.server.JBossASEmbeddedServer;
import org.jboss.embedded.core.server.JBossASEmbeddedServerImpl;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.tmpdpl.api.container.DeploymentException;

public class JbossEmbeddedContainer implements Controlable, Deployer
{
   private JBossASEmbeddedServer server;
   
   public JbossEmbeddedContainer()
   {
      server = new JBossASEmbeddedServerImpl();
   }

   @Override
   public void start() throws Exception
   {
      server.initialize();
      server.start();
   }
   
   @Override
   public void stop() throws Exception
   {
      server.shutdown();
   }
   
   @Override
   public void deploy(Archive<?> archive) throws DeploymentException
   {
      server.deploy(archive);
   }
   
   @Override
   public void undeploy(Archive<?> archive) throws DeploymentException
   {
      server.undeploy(archive);
   }
}
