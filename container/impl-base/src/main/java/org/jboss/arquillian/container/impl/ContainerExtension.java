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
package org.jboss.arquillian.container.impl;

import org.jboss.arquillian.container.impl.client.ContainerDeploymentContextHandler;
import org.jboss.arquillian.container.impl.client.container.ContainerDeployController;
import org.jboss.arquillian.container.impl.client.container.ContainerLifecycleController;
import org.jboss.arquillian.container.impl.client.container.ContainerRegistryCreator;
import org.jboss.arquillian.container.impl.client.container.DeploymentExceptionHandler;
import org.jboss.arquillian.container.impl.client.deployment.ArchiveDeploymentExporter;
import org.jboss.arquillian.container.impl.context.ContainerContextImpl;
import org.jboss.arquillian.container.impl.context.DeploymentContextImpl;
import org.jboss.arquillian.container.spi.ServerKillProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * ContainerExtension
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerExtension implements LoadableExtension
{
   @Override
   public void register(ExtensionBuilder builder)
   {
      builder.context(ContainerContextImpl.class)
             .context(DeploymentContextImpl.class);
      
      builder.observer(ContainerRegistryCreator.class)
             .observer(ContainerDeploymentContextHandler.class)
             .observer(ContainerLifecycleController.class)
             .observer(ContainerDeployController.class)
             .observer(ArchiveDeploymentExporter.class)
             .observer(DeploymentExceptionHandler.class);
   }

}
