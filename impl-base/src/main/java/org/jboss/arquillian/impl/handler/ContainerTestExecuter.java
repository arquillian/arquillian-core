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
package org.jboss.arquillian.impl.handler;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.impl.event.EventHandler;
import org.jboss.arquillian.impl.event.type.Test;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.TestResult;

/**
 * A Handler for executing the remote Test Method.<br/>
 * <br/>
 *
 *  <b>Imports:</b><br/>
 *   {@link DeployableContainer}<br/>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * @see DeployableContainer
 */
public class ContainerTestExecuter implements EventHandler<TestContext, Test>
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.event.EventHandler#callback(java.lang.Object, java.lang.Object)
    */
   @Override
   public void callback(TestContext context, Test event) throws Exception
   {
      ContainerMethodExecutor executor = context.get(ContainerMethodExecutor.class);
      Validate.stateNotNull(executor, "No " + ContainerMethodExecutor.class.getName() + " found in context");
      
      TestResult result = executor.invoke(event.getTestMethodExecutor());
      
      // TODO: move to context ? 
      event.setTestResult(result);
   }
}
