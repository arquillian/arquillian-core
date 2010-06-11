package org.jboss.weld.deployments.beans.message;

import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven
public class NoViewMessageEJB implements MessageListener
{
   boolean pinged;

   @Override
   public void onMessage(Message message)
   {
      pinged = true;
   }

   public boolean isPinged()
   {
      return pinged;
   }

}
