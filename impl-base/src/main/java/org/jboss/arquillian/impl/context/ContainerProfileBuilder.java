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

import org.jboss.arquillian.impl.handler.TestCaseEnricher;
import org.jboss.arquillian.impl.handler.TestEventExecuter;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.Test;

/**
 * ContainerProfileBuilder
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerProfileBuilder implements ProfileBuilder
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ProfileBuilder#buildSuiteContext(org.jboss.arquillian.impl.context.SuiteContext)
    */
   public void buildSuiteContext(SuiteContext context) 
   {
// TODO: create configuration option to turn on/off time ?
//      EventHandler<SuiteEvent> timer = new ExecutionTimer();
//      context.register(BeforeSuite.class, timer);
//      context.register(AfterSuite.class, timer);
//      context.register(BeforeClass.class, timer);
//      context.register(AfterClass.class, timer);
//      context.register(Before.class, timer);
//      context.register(Test.class, timer);
//      context.register(After.class, timer);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ProfileBuilder#buildClassContext(org.jboss.arquillian.impl.context.ClassContext, java.lang.Class)
    */
   public void buildClassContext(ClassContext context, Class<?> testClass)
   {
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ProfileBuilder#buildTestContext(org.jboss.arquillian.impl.context.TestContext, java.lang.Object)
    */
   public void buildTestContext(TestContext context, Object testInstance)
   {
      context.register(Before.class, new TestCaseEnricher());
      context.register(Test.class, new TestEventExecuter());
   }
}
