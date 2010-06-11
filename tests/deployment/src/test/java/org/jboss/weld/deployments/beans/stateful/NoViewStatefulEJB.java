package org.jboss.weld.deployments.beans.stateful;

import javax.ejb.Stateful;
import javax.enterprise.inject.Model;

@Stateful
@Model
public class NoViewStatefulEJB
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
