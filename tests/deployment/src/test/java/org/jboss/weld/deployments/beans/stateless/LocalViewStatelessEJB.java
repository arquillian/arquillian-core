package org.jboss.weld.deployments.beans.stateless;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.jboss.weld.deployments.beans.LocalI;

@Stateless
@Named
public class LocalViewStatelessEJB implements LocalI
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
