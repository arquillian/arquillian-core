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
package org.jboss.arquillian.impl.bootstrap;

import org.jboss.arquillian.impl.core.spi.context.ClassContext;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.impl.core.spi.context.TestContext;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.After;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.AfterSuite;

/**
 * SuiteContextActivator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContextDeActivator
{
   @Inject
   public Instance<SuiteContext> suiteContext;

   @Inject
   public Instance<ClassContext> classContext;
   
   @Inject
   public Instance<TestContext> testContext;

   public void deactivateSuite(@Observes AfterSuite event)
   {
      suiteContext.get().deactivate();
      suiteContext.get().destroy();
   }

   public void deactivateClass(@Observes AfterClass event)
   {
      classContext.get().deactivate();
      classContext.get().destroy(event.getTestClass().getJavaClass());
   }
   
   public void deactivateTest(@Observes After event)
   {
      testContext.get().deactivate();
      testContext.get().destroy(event.getTestInstance());
   }
}
