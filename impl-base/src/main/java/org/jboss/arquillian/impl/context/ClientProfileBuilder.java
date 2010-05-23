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

import org.jboss.arquillian.impl.handler.ActivateRunModeTypeDeployment;
import org.jboss.arquillian.impl.handler.ActivateRunModeTypeClient;
import org.jboss.arquillian.impl.handler.ArchiveDeploymentExporter;
import org.jboss.arquillian.impl.handler.ArchiveGenerator;
import org.jboss.arquillian.impl.handler.ContainerCreator;
import org.jboss.arquillian.impl.handler.ContainerDeployer;
import org.jboss.arquillian.impl.handler.ContainerStarter;
import org.jboss.arquillian.impl.handler.ContainerStopper;
import org.jboss.arquillian.impl.handler.ContainerTestExecuter;
import org.jboss.arquillian.impl.handler.ContainerUndeployer;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.spi.event.suite.Test;

/**
 * ClientContextCreator
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClientProfileBuilder implements ProfileBuilder
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

      context.register(BeforeSuite.class, new ContainerCreator());
      context.register(BeforeSuite.class, new ContainerStarter());
      context.register(AfterSuite.class, new ContainerStopper());
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ProfileBuilder#buildClassContext(org.jboss.arquillian.impl.context.ClassContext, java.lang.Class)
    */
   public void buildClassContext(ClassContext context, Class<?> testClass)
   {
      /*
       * If RunMode AS_CLIENT a local DeploymentGenerator that returns the ApplicationArchive is bound to the context,
       * else the ClientDeploymentGenerator is used. 
       */
      context.register(BeforeClass.class, new ActivateRunModeTypeDeployment());

      context.register(BeforeClass.class, new ArchiveGenerator());
      context.register(BeforeClass.class, new ContainerDeployer());
      context.register(AfterClass.class, new ContainerUndeployer());

      context.register(BeforeClass.class, new ActivateRunModeTypeClient());
      
      context.register(BeforeClass.class, new ArchiveDeploymentExporter());
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ProfileBuilder#buildTestContext(org.jboss.arquillian.impl.context.TestContext, java.lang.Object)
    */
   public void buildTestContext(TestContext context, Object testInstance)
   {
      context.register(Test.class, new ContainerTestExecuter());
   }
}