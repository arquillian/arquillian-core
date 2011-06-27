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

import org.jboss.arquillian.container.test.impl.client.ContainerEventController;
import org.jboss.arquillian.container.test.impl.client.LocalCommandService;
import org.jboss.arquillian.container.test.impl.client.container.ClientContainerControllerCreator;
import org.jboss.arquillian.container.test.impl.client.container.ContainerRestarter;
import org.jboss.arquillian.container.test.impl.client.container.command.ContainerCommandObserver;
import org.jboss.arquillian.container.test.impl.client.deployment.ClientDeployerCreator;
import org.jboss.arquillian.container.test.impl.client.deployment.DeploymentGenerator;
import org.jboss.arquillian.container.test.impl.client.deployment.command.DeploymentCommandObserver;
import org.jboss.arquillian.container.test.impl.client.deployment.tool.ArchiveDeploymentToolingExporter;
import org.jboss.arquillian.container.test.impl.client.protocol.ProtocolRegistryCreator;
import org.jboss.arquillian.container.test.impl.client.protocol.local.LocalProtocol;
import org.jboss.arquillian.container.test.impl.deployment.ArquillianDeploymentAppender;
import org.jboss.arquillian.container.test.impl.enricher.resource.ArquillianResourceTestEnricher;
import org.jboss.arquillian.container.test.impl.execution.ClientBeforeAfterLifecycleEventExecuter;
import org.jboss.arquillian.container.test.impl.execution.ClientTestExecuter;
import org.jboss.arquillian.container.test.impl.execution.LocalTestExecuter;
import org.jboss.arquillian.container.test.impl.execution.RemoteTestExecuter;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.command.CommandService;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.impl.TestContextHandler;
import org.jboss.arquillian.test.impl.context.ClassContextImpl;
import org.jboss.arquillian.test.impl.context.SuiteContextImpl;
import org.jboss.arquillian.test.impl.context.TestContextImpl;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * ContainerTestExtension
 * 
 * This Extension Overrides the original TestExtension. Needed to change the behavior of TestEnricher to be RunMode aware
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerTestExtension implements LoadableExtension
{
   @Override
   public void register(ExtensionBuilder builder)
   {
      // Start -> Copied from TestExtension
      builder.context(SuiteContextImpl.class)
             .context(ClassContextImpl.class)
             .context(TestContextImpl.class);

      builder.observer(TestContextHandler.class)
             .observer(ClientTestInstanceEnricher.class);

      // End -> Copied from TestExtension
      
      builder.service(AuxiliaryArchiveAppender.class, ArquillianDeploymentAppender.class)
             .service(TestEnricher.class, ArquillianResourceTestEnricher.class)
             .service(Protocol.class, LocalProtocol.class)
             .service(CommandService.class, LocalCommandService.class);
      
      builder.observer(ContainerEventController.class)
             .observer(ContainerRestarter.class)
             .observer(DeploymentGenerator.class)
             .observer(ArchiveDeploymentToolingExporter.class)
             .observer(ProtocolRegistryCreator.class)
             .observer(ClientContainerControllerCreator.class)
             .observer(ClientDeployerCreator.class)
             .observer(ClientBeforeAfterLifecycleEventExecuter.class)
             .observer(ClientTestExecuter.class)
             .observer(LocalTestExecuter.class)
             .observer(RemoteTestExecuter.class)
             .observer(DeploymentCommandObserver.class)
             .observer(ContainerCommandObserver.class);
   }

}
