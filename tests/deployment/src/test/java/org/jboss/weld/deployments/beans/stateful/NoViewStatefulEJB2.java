package org.jboss.weld.deployments.beans.stateful;

import javax.ejb.Stateful;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

@Stateful
@Model
public class NoViewStatefulEJB2
{
   @Inject
   NoViewStatefulEJB ejb;
   
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
