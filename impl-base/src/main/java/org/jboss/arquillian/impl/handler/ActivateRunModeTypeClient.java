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

import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * Handler that will setup the context as defined by the {@link RunModeType#AS_CLIENT}. <br/>
 * Only activates local run mode if the TestClass is annotated with a {@link RunModeType#AS_CLIENT} {@link Run}.<br/> 
 * <br/>  
 * 
 *  <b>Exports:</b><br/>
 *   {@link ContainerMethodExecutor}<br/>
 *   
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ActivateRunModeTypeClient extends AbstractRunModeHandler<BeforeClass>
{
   
   @Override
   protected void hasClientRunMode(Context context)
   {
      context.add(ContainerMethodExecutor.class, new LocalMethodExecutor());      
   }

   @Override
   protected void hasContainerRunMode(Context context)
   {
      // NO-OP, Use the ContainerMethodExecutor provided by the DeployableContainer
   }
   
   // TODO: this is a copy of the protocol-local Executor. Move to SPI and remove protocol local? 
   static class LocalMethodExecutor implements ContainerMethodExecutor 
   {
      /* (non-Javadoc)
       * @see org.jboss.arquillian.spi.ContainerMethodExecutor#invoke(org.jboss.arquillian.spi.TestMethodExecutor)
       */
      public TestResult invoke(TestMethodExecutor testMethodExecutor)
      {
         TestResult result = new TestResult();
         try 
         {
            testMethodExecutor.invoke();
            result.setStatus(Status.PASSED);
         }
         catch (final Throwable e) 
         {
            result.setStatus(Status.FAILED);
            result.setThrowable(e);
         } 
         finally 
         {
            result.setEnd(System.currentTimeMillis());
         }
         return result;
      }
   }
}