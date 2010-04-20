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

import org.jboss.arquillian.impl.event.type.Before;
import org.jboss.arquillian.impl.event.type.Test;
import org.jboss.arquillian.impl.handler.TestEventExecuter;
import org.jboss.arquillian.impl.handler.TestCaseEnricher;

/**
 * ClientContextCreator
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerProfileBuilder implements ProfileBuilder
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ContextBuilder#buildSuiteContext(org.jboss.arquillian.impl.context.SuiteContext)
    */
   @Override
   public void buildSuiteContext(SuiteContext context) 
   {
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ContextBuilder#buildClassContext(org.jboss.arquillian.impl.context.ClassContext)
    */
   @Override
   public void buildClassContext(ClassContext context)
   {
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ContextBuilder#buildTestContext(org.jboss.arquillian.impl.context.TestContext)
    */
   @Override
   public void buildTestContext(TestContext context)
   {
      context.register(Before.class, new TestCaseEnricher());
      context.register(Test.class, new TestEventExecuter());
   }
}
