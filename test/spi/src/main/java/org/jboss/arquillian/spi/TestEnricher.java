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
 * SPI used to enrich the runtime test object.
 *
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface TestEnricher
{
   /**
    * Extension point to add features to the a Test class instance.<br/>
    * <br/>
    * IE. Instance field injection
    *  
    * @param testCase The test case instance
    */
   void enrich(Object testCase);
   
   /**
    * Extension point to add features to the test method arguments.<br/>
    * <br/>
    * IE. Argument injection<br/>
    * <br/>
    * 
    * The return value Object[] must match the Field[] indexes. 
    * Leave Object[] index as null if it can't be handled by this {@link TestEnricher}.
    * 
    * @param method
    * @return A Object[] of Arguments 
    */
   Object[] resolve(Method method);
}
