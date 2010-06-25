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

import org.jboss.arquillian.spi.TestClass;


/**
 * Base for events fired in the Test Class execution cycle.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClassEvent extends SuiteEvent
{
   private TestClass testClass;
   
   /**
    * @param testClass The Test case {@link Class}
    * @throws IllegalArgumentException if testCase is null 
    */
   public ClassEvent(Class<?> testClass)
   {
      this(new TestClass(testClass));
   }
   
   /**
    * @param testClass The Test case {@link Class}
    * @throws IllegalArgumentException if testCase is null 
    */
   public ClassEvent(TestClass testClass)
   {
      Validate.notNull(testClass, "TestClass must be specified");
      
      this.testClass = testClass;
   }
   
   public TestClass getTestClass()
   {
      return testClass;
   }
}
