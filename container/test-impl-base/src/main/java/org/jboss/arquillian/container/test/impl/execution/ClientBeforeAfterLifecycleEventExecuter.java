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
package org.jboss.arquillian.container.test.impl.execution;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.test.impl.RunModeUtils;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.LifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;

/**
 * Observer that executes the Before / After phases on the test case if the current test is in RunMode Client. <br/>
 * <br/>
 * BeforeClass/AfterClass are ALWAYS executed on Client side and NEVER In Container. <br/>
 * In Container does not keep state between @Test, so Before/AfterClass works the same as Before/After.<br/>
 * <br/>
 * Before/After are ONLY executed on Client side if the @Test's RunMode is Client. <br/>
 * <br/>
 * 
 * BeforeX event execution has a low precedence to execute as late in the Before Phase as possible.<br/>
 * AfterX event execution has a high precedence to execute as early in the After Phase as possible.<br/>
 * (compared to other Arquillian @Observers)<br/>
 * 
 * 
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * @see BeforeLifecycleEventExecuter
 */
public class ClientBeforeAfterLifecycleEventExecuter
{
   @Inject
   private Instance<Deployment> deployment;
   
   @Inject
   private Instance<Container> container;

   public void on(@Observes(precedence = -100) BeforeClass event) throws Throwable
   {
      execute(event);
   }

   public void on(@Observes(precedence = 100) AfterClass event) throws Throwable
   {
      execute(event);
   }

   public void on(@Observes(precedence = -100) BeforeTestLifecycleEvent event) throws Throwable
   {
      if(isRunAsClient(event) || isLocalContainer())
      {
         execute(event);         
      }
   }

   public void on(@Observes(precedence = 100) AfterTestLifecycleEvent event) throws Throwable
   {
      if(isRunAsClient(event) || isLocalContainer())
      {
         execute(event);         
      }
   }

   private boolean isRunAsClient(TestLifecycleEvent event)
   {
      return RunModeUtils.isRunAsClient(
            deployment.get(),
            event.getTestClass().getJavaClass(), 
            event.getTestMethod());
   }

   private boolean isLocalContainer()
   {
      return RunModeUtils.isLocalContainer(container.get());
   }
   
   private void execute(LifecycleEvent event) throws Throwable
   {
      event.getExecutor().invoke();
   }
}
