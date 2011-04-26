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

/**
 * Base for events fired in the Test execution cycle.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestEvent extends ClassEvent
{
   private Object testInstance;
   private Method testMethod;

   /**
    * @param testInstance The test case instance
    * @param testMethod The test method 
    * @throws IllegalArgumentException if testInstance is null
    * @throws IllegalArgumentException if testMethod is null
    */
   public TestEvent(Object testInstance, Method testMethod)
   {
      super(validateAndExtractClass(testInstance, testMethod));
      
      this.testInstance = testInstance;
      this.testMethod = testMethod;
   }

   // TODO: eeehh..? 
   private static Class<?> validateAndExtractClass(Object testInstance, Method testMethod) 
   {
      Validate.notNull(testInstance, "TestInstance must be specified");
      Validate.notNull(testMethod, "TestMethod must be specified");
      
      return testInstance.getClass();
   }
   
   public Object getTestInstance()
   {
      return testInstance;
   }
   
   public Method getTestMethod()
   {
      return testMethod;
   }
}
