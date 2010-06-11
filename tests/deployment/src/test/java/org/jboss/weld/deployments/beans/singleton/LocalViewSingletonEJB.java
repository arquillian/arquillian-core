package org.jboss.weld.deployments.beans.singleton;

import javax.ejb.Singleton;
import javax.inject.Named;

import org.jboss.weld.deployments.beans.LocalI;

@Singleton
@Named
public class LocalViewSingletonEJB implements LocalI
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
