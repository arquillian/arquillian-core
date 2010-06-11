package org.jboss.weld.deployments.beans.stateful;

import javax.ejb.Stateful;
import javax.enterprise.inject.Model;

import org.jboss.weld.deployments.beans.LocalI;

@Stateful
@Model
public class LocalViewStatefulEJB implements LocalI
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
