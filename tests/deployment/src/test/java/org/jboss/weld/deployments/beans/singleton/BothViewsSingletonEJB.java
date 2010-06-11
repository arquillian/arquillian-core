package org.jboss.weld.deployments.beans.singleton;

import javax.ejb.Singleton;
import javax.inject.Named;

import org.jboss.weld.deployments.beans.LocalI;
import org.jboss.weld.deployments.beans.RemoteI;

@Singleton
@Named
public class BothViewsSingletonEJB implements LocalI, RemoteI
{
   boolean pinged;

   public void ping()
   {
      pinged = true;
   }

   public boolean isPinged()
   {
      return pinged;
   }

}
