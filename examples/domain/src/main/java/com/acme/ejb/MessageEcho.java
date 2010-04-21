/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.ejb;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * MessageEcho
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@MessageDriven(activationConfig = {
      @ActivationConfigProperty( propertyName = "destination", propertyValue = "queue/DLQ"),
      @ActivationConfigProperty( propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class MessageEcho implements MessageListener
{

   @Resource(mappedName = "java:/ConnectionFactory")
   private ConnectionFactory factory;
   
   public void onMessage(Message msg)
   {
      try 
      {
         System.out.println("received " + msg.getJMSMessageID());
         
         Connection connection = factory.createConnection();
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = session.createProducer(msg.getJMSReplyTo());
         producer.send(msg);
         producer.close();
         session.close();
         connection.close();
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not reply to message", e);
      }
   }
}
