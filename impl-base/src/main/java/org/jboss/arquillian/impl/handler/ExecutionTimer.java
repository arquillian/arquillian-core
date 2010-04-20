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
package org.jboss.arquillian.impl.handler;

import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.event.EventHandler;
import org.jboss.arquillian.impl.event.type.SuiteEvent;

/**
 * A Handler for simple execution time tracking. Prints to System.out the 
 * time in milliseconds since last execution. 
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ExecutionTimer implements EventHandler<SuiteContext, SuiteEvent>
{
   private long start = System.currentTimeMillis();
   private long previous = System.currentTimeMillis();
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.event.EventHandler#callback(java.lang.Object, java.lang.Object)
    */
   @Override
   public void callback(SuiteContext context, SuiteEvent event) throws Exception 
   {
      System.out.println(DeployableTestBuilder.getProfile() + "-" + event.getClass().getName() + " " + (previous - start) + " ms");
      start = previous;
      previous = System.currentTimeMillis();
   }
}
