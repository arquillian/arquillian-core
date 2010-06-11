package org.jboss.weld.deployments.beans.singleton;

import javax.ejb.Singleton;
import javax.inject.Named;

@Singleton
@Named
public class NoViewSingletonEJB
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
