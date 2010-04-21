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
package org.jboss.arquillian.protocol.local;

import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.util.TestEnrichers;

/**
 * LocalMethodExecutor
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LocalMethodExecutor implements ContainerMethodExecutor
{

   public TestResult invoke(TestMethodExecutor testMethodExecutor)
   {
      try 
      {
         TestEnrichers.enrich(testMethodExecutor.getInstance());

         testMethodExecutor.invoke();
         
         return new TestResult()
         {
            private static final long serialVersionUID = 1L;

            public Throwable getThrowable() { return null; }
            
            public Status getStatus() { return Status.PASSED; }
         };
      }
      catch (final Throwable e) 
      {
         return new TestResult() 
         {
            private static final long serialVersionUID = 1L;

            public Status getStatus() {return Status.FAILED; }
            
            public Throwable getThrowable() {return e;}
         };
      }
   }
}