package org.jboss.weld.deployments.beans;

import java.io.Serializable;

import javax.ejb.Remote;

@Remote
public interface RemoteI extends Serializable
{
   public void ping();
   public boolean isPinged();
}
