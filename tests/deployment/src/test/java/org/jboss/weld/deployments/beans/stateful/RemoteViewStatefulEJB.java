package org.jboss.weld.deployments.beans.stateful;

import javax.ejb.Stateful;
import javax.enterprise.inject.Model;

import org.jboss.weld.deployments.beans.RemoteI;

@Stateful
@Model
public class RemoteViewStatefulEJB implements RemoteI
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
