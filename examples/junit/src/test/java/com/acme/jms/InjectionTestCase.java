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
package com.acme.jms;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.acme.ejb.MessageEcho;
import com.acme.util.jms.QueueRequestor;

/**
 * JmsTest
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class InjectionTestCase
{
   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class)
            .addClasses(
                  MessageEcho.class, 
                  QueueRequestor.class);
   }
   
   @Resource(mappedName = "/queue/DLQ") 
   private Queue dlq;
   
   @Resource(mappedName = "/ConnectionFactory") 
   private ConnectionFactory factory;
   
   @Test
   public void shouldBeAbleToSendMessage() throws Exception {
      
      String messageBody = "ping";
      
      Connection connection = null;
      try
      {
         connection = factory.createConnection();
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         QueueRequestor requestor = new QueueRequestor((QueueSession)session, dlq);

         connection.start();
         
         Message request = session.createTextMessage(messageBody);
         Message response = requestor.request(request, 5000);
         
         Assert.assertEquals(
               "Should have responded with same message",
               messageBody,
               ((TextMessage)response).getText());
      }
      finally
      {
         connection.close();
      }
   }
}
