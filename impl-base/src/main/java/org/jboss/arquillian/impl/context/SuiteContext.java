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
package org.jboss.arquillian.impl.context;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.impl.event.type.SuiteEvent;
import org.jboss.arquillian.spi.ServiceLoader;

/**
 * A SuiteContext is alive in the following life cycles: <br/>
 *  <ul>
 *  <li>BeforeSuite</li>
 *  <li>BeforeClass</li>
 *  <li>Before</li>
 *  <li>Test</li>
 *  <li>After</li>
 *  <li>AfterClass</li>
 *  <li>AfterSuite</li>
 *  </ul>
 * Used for firing events and storing of object that live in the Suite Scope.
 *   
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class SuiteContext extends AbstractEventContext<SuiteContext, SuiteEvent>
{
   /**
    * Create a new SuiteContext.
    * 
    * @param serviceLoader The ServiceLoader to use
    * @throws IllegalArgumentException if serviceLoader is null
    */
   public SuiteContext(final ServiceLoader serviceLoader)
   {
      Validate.notNull(serviceLoader, "ServiceLoader must be specified");
      
      add(ServiceLoader.class, serviceLoader);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.Context#fire(org.jboss.arquillian.impl.event.Event)
    */
   @Override
   public void fire(SuiteEvent event)
   {
      getEventManager().fire(this, event);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.Context#getParentContext()
    */
   /**
    * @return Always null, SuiteContext is the root context.
    */
   @Override
   public Context<?, ?> getParentContext()
   {
      return null;
   }
}
