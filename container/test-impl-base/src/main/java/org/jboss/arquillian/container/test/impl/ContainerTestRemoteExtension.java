/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.test.impl;

import org.jboss.arquillian.container.test.impl.client.container.ContainerContainerControllerCreator;
import org.jboss.arquillian.container.test.impl.client.deployment.ContainerDeployerCreator;
import org.jboss.arquillian.container.test.impl.enricher.resource.ArquillianResourceTestEnricher;
import org.jboss.arquillian.container.test.impl.execution.AfterLifecycleEventExecuter;
import org.jboss.arquillian.container.test.impl.execution.BeforeLifecycleEventExecuter;
import org.jboss.arquillian.container.test.impl.execution.ContainerTestExecuter;
import org.jboss.arquillian.container.test.impl.execution.LocalTestExecuter;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.impl.TestExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * ContainerTestExtension
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerTestRemoteExtension extends TestExtension implements RemoteLoadableExtension
{
   @Override
   public void register(ExtensionBuilder builder)
   {
      super.register(builder);
      
      builder.service(TestEnricher.class, ArquillianResourceTestEnricher.class);
      
      builder.observer(AfterLifecycleEventExecuter.class)
             .observer(ContainerTestExecuter.class)
             .observer(ContainerDeployerCreator.class)
             .observer(ContainerContainerControllerCreator.class)
             .observer(LocalTestExecuter.class)
             .observer(BeforeLifecycleEventExecuter.class);
   }

}
