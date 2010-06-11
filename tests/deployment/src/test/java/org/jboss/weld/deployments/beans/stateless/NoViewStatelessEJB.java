package org.jboss.weld.deployments.beans.stateless;

import javax.ejb.Stateless;
import javax.inject.Named;

@Stateless
@Named
public class NoViewStatelessEJB
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
