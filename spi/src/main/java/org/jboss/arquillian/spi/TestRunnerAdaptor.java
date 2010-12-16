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
package org.jboss.arquillian.spi;

import java.lang.reflect.Method;

/**
 * TestRunnerAdaptor
 * 
 * Need to be Thread-safe
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface TestRunnerAdaptor
{
//   /*
//    *  TODO: reevaluate this. a  basic stack that will peek at the top, suite/class/test context.
//    *  used to fetch the context outside the test adaptor inside the test framework integration to 
//    *  do method argumnet injection. 
//    */
//   /**
//    * @return
//    */
//   Context getActiveContext();
//   
   /**
    * @throws Exception
    */
   void beforeSuite() throws Exception; 
   
   /**
    * @throws Exception
    */
   void afterSuite() throws Exception;

   /**
    * @param testClass
    * @throws Exception
    */
   void beforeClass(Class<?> testClass) throws Exception;
   
   /**
    * @param testClass
    * @throws Exception
    */
   void afterClass(Class<?> testClass) throws Exception;
   
   /**
    * @param testInstance
    * @param testMethod
    * @throws Exception
    */
   void before(Object testInstance, Method testMethod) throws Exception;
   
   /**
    * @param testInstance
    * @param testMethod
    * @throws Exception
    */
   void after(Object testInstance, Method testMethod) throws Exception;

   /**
    * @param testMethodExecutor
    * @return
    * @throws Exception
    */
   TestResult test(TestMethodExecutor testMethodExecutor) throws Exception;
   
   /**
    * Shutdown Arquillian cleanly.  
    */
   void shutdown();
}
