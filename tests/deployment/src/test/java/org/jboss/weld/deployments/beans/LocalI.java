package org.jboss.weld.deployments.beans;

import javax.ejb.Local;

@Local
public interface LocalI
{
   public void ping();
   public boolean isPinged();
}
