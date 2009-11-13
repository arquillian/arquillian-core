package org.jboss.arquillian.api.container;

import org.jboss.arquillian.api.Deployer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.tmpdpl.api.container.DeploymentException;
import org.jboss.tmpdpl.api.shrinkwrap.container.ArchiveContainer;

public class ContainerDeployer implements Deployer
{
   private ArchiveContainer container;
   
   public ContainerDeployer(ArchiveContainer container)
   {
      this.container = container;
   }

   public void deploy(Archive<?> archive) throws DeploymentException
   {
      if(archive == null) 
      {
         throw new IllegalArgumentException("Can not deploy null artifact");
      }
      try 
      {
         container.deploy(archive);
      } 
      catch (Exception e) 
      {
         throw new DeploymentException("Could not deploy artifact " + archive.getName(), e);
      }
   }

   public void undeploy(Archive<?> archive) throws DeploymentException
   {
      if(archive == null) 
      {
         throw new IllegalArgumentException("Can not undeploy null artifact");
      }
      try 
      {
         container.undeploy(archive);
      } 
      catch (Exception e) 
      {
         throw new DeploymentException("Could not undeploy artifact " + archive.getName(), e);
      }
   }
}
