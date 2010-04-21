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

import org.jboss.arquillian.impl.TestResultImpl;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.impl.event.EventHandler;
import org.jboss.arquillian.impl.event.type.Test;
import org.jboss.arquillian.spi.TestResult.Status;

/**
 * A Handler for executing the Test Method.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestEventExecuter implements EventHandler<TestContext, Test>
{
   
   public void callback(TestContext context, Test event) throws Exception 
   {
      try 
      {
         event.getTestMethodExecutor().invoke();
         event.setTestResult(new TestResultImpl(Status.PASSED));
      } 
      catch (Throwable e) 
      {
         event.setTestResult(new TestResultImpl(Status.FAILED, e));
      }
   }
}
