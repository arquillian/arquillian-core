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
package org.jboss.arquillian.spi.event.suite;

import java.lang.reflect.Method;

import org.jboss.arquillian.spi.LifecycleMethodExecutor;

/**
 * Event fired After the Test method execution.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class After extends TestLifecycleEvent
{
   /**
    * @param testInstance The test case instance being tested
    * @param testMethod The test method that is about to be executed
    */
   public After(Object testInstance, Method testMethod)
   {
      super(testInstance, testMethod);
   }

   /**
    * @param testInstance The test case instance being tested
    * @param testMethod The test method that was executed
    * @param executor A call back when the LifecycleMethod represented by this event should be invoked
    */
   public After(Object testInstance, Method testMethod, LifecycleMethodExecutor executor)
   {
      super(testInstance, testMethod, executor);
   }
}
