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
package org.jboss.arquillian.protocol.jmx;

import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;

/**
 * JMXMethodExecutor
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JMXMethodExecutor implements ContainerMethodExecutor
{
   @Override
   public TestResult invoke(TestMethodExecutor testMethodExecutor)
   {
      TestResult result = new TestResult();
      try 
      {
         testMethodExecutor.invoke();
         result.setStatus(Status.PASSED);
      }
      catch (final Throwable e) 
      {
         /*
          *  TODO: the internal state TestResult is FAILED with Exception set, but it might have passed
          *  due to the TestFrameworks ExpectedExceptions. We need to know this information to set the correct state.
          */

         result.setStatus(Status.FAILED);
         result.setThrowable(e);
      }
      result.setEnd(System.currentTimeMillis());
      return result;
   }
}