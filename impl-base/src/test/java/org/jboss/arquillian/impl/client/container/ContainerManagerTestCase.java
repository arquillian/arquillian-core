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
package org.jboss.arquillian.impl.client.container;

import java.util.ServiceLoader;

import org.jboss.arquillian.impl.DynamicServiceLoader;
import org.jboss.arquillian.impl.configuration.ContainerDefImpl;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;


/**
 * ContainerManagerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerManagerTestCase
{
   @Test
   @Ignore // broken
   public void shouldBeAbleToCreateAIsolatedContainerClassloader() throws Exception
   {
      ContainerRegistry manager = new ContainerRegistry();
      Container container = manager.create(
            new ContainerDefImpl().setContainerName("Weld-SE-Embedded")
            .dependency("org.jboss.arquillian.container:arquillian-weld-se-embedded-1.1:1.0.0-SNAPSHOT")
            .dependency("org.jboss.weld:weld-core:1.1.0.Beta1")
            .dependency("org.slf4j:slf4j-simple:1.5.10")
            .dependency("javax.el:el-api:2.2"),
            new DynamicServiceLoader());
   
      ClassLoader classLoader = container.getClassLoader();
      
      DeployableContainer<?> deployableContainer = ServiceLoader.load(DeployableContainer.class, classLoader).iterator().next();
      ContainerConfiguration containerConfiguration = ServiceLoader.load(ContainerConfiguration.class, classLoader).iterator().next();
      
      Configuration configuration = new Configuration();
      configuration.addContainerConfig(containerConfiguration);
      
      System.out.println(deployableContainer);
      
      Thread.currentThread().setContextClassLoader(classLoader);

      Archive<?> deployment = ShrinkWrap.create(JavaArchive.class)
            .addManifestResource(EmptyAsset.INSTANCE, "beans.xml");

      deployableContainer.deploy(deployment);
      deployableContainer.undeploy(deployment);
   }
}
