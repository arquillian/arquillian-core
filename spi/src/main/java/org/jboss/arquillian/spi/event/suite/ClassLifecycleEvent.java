/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.arquillian.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.spi.TestClass;

/**
 * A ClassLifeCycleEvent is a type of ClassEvent used for e.g. @BeforeClass/@AfterClass operations on the
 * TestClass.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClassLifecycleEvent extends ClassEvent implements LifecycleEvent
{
   private LifecycleMethodExecutor executor;
   
   /**
    * Create a new ClassLifecycleEvent for a specific Class. <br/>
    * <br/>
    * This will use a NO_OP LifecycleMethodExecutor. 
    *  
    * @param testClass The source for this ClassEvent
    */
   public ClassLifecycleEvent(Class<?> testClass)
   {
      this(testClass, LifecycleMethodExecutor.NO_OP);
   }
   
   /**
    * Create a new ClassLifecycleEvent for a specific Class. <br/>
    * <br/>
    * This will use a NO_OP LifecycleMethodExecutor. 
    *  
    * @param testClass The source for this ClassEvent
    */
   public ClassLifecycleEvent(TestClass testClass)
   {
      this(testClass, LifecycleMethodExecutor.NO_OP);
   }
   
   /**
    * Create a new ClassLifecycleEvent for a specific Class. <br/>
    *  
    * @param testClass The source for this ClassEvent
    * @param executor A call back when the LifecycleMethod represented by this event should be invoked
    */
   public ClassLifecycleEvent(Class<?> testClass, LifecycleMethodExecutor executor)
   {
      super(testClass);  
      
      Validate.notNull(executor, "LifecycleMethodExecutor must be specified");
      this.executor = executor;
   }

   /**
    * Create a new ClassLifecycleEvent for a specific Class. <br/>
    *  
    * @param testClass The source for this ClassEvent
    * @param executor A call back when the LifecycleMethod represented by this event should be invoked
    */
   public ClassLifecycleEvent(TestClass testClass, LifecycleMethodExecutor executor)
   {
      super(testClass);  
      
      Validate.notNull(executor, "LifecycleMethodExecutor must be specified");
      this.executor = executor;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.event.suite.LifecycleEvent#getExecutor()
    */
   public LifecycleMethodExecutor getExecutor()
   {
      return executor;
   }
}
