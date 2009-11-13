package org.jboss.arquillian.api;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.tmpdpl.api.container.DeploymentException;

public interface Deployer
{

   void deploy(Archive<?> archive) throws DeploymentException;
   void undeploy(Archive<?> archive) throws DeploymentException;

}
