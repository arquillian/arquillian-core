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
package org.jboss.arquillian.impl.domain;

import junit.framework.Assert;

import org.jboss.arquillian.impl.configuration.ContainerDefImpl;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * DomainModelTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerRegistryTestCase
{
   @Mock
   private ServiceLoader serviceLoader;

   @Mock
   private DeployableContainer<DummyContainerConfiguration> deployableContainer;

   @Before
   public void setup() throws Exception
   {
      Mockito.when(serviceLoader.onlyOne(Mockito.isA(ClassLoader.class), Mockito.same(DeployableContainer.class))).thenReturn(deployableContainer);
      Mockito.when(deployableContainer.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
   }
   
   @Test
   public void shouldBeAbleToDefaultTargetToOnlyRegisteredContainer() throws Exception
   {
      String name = "some-name";
      
      ContainerRegistry registry = new ContainerRegistry();
      registry.create(new ContainerDefImpl().setContainerName(name), serviceLoader);
      
      Container container = registry.getContainer(TargetDescription.DEFAULT);
      
      Assert.assertEquals(
            "Verify that the only registered container is returned as default",
            name, container.getName());
   }

   @Test
   public void shouldBeAbleToDefaultTargetToDefaultRegisteredContainer() throws Exception
   {
      String name = "some-name";
      
      ContainerRegistry registry = new ContainerRegistry();
      registry.create(new ContainerDefImpl().setContainerName("some-other-name"), serviceLoader);
      registry.create(new ContainerDefImpl().setContainerName(name).setDefault(), serviceLoader);
      
      Container container = registry.getContainer(TargetDescription.DEFAULT);
      
      Assert.assertEquals(
            "Verify that the default registered container is returned as default",
            name, container.getName());
   }

   @Test
   public void shouldBeAbleToCreateContainerConfiguration() throws Exception
   {
      String name = "some-name";
      String prop = "prop-value";
      
      ContainerRegistry registry = new ContainerRegistry();
      registry.create(new ContainerDefImpl().setContainerName(name)
                           .property("property", prop), serviceLoader);
      
      Container container = registry.getContainer(new TargetDescription(name));
      
      Assert.assertEquals(
            "Verify that the only registered container is returned as default",
            name, container.getName());

      Assert.assertEquals(
            "Verify that the configuration was populated",
            prop, 
            ((DummyContainerConfiguration)container.createDeployableConfiguration()).getProperty());
   }
   
   @Test
   public void shouldBeAbleToSpecifyTarget() throws Exception
   {
      String name = "some-name";
      
      ContainerRegistry registry = new ContainerRegistry();
      registry.create(new ContainerDefImpl().setContainerName("other-name"), serviceLoader);
      registry.create(new ContainerDefImpl().setContainerName(name), serviceLoader);
      
      Container container = registry.getContainer(new TargetDescription(name));
      
      Assert.assertEquals(
            "Verify that the specific registered container is returned",
            name, container.getName());
   }

   public static class DummyContainerConfiguration implements ContainerConfiguration
   {
      private String property;
      
      /**
       * @param property the property to set
       */
      public void setProperty(String property)
      {
         this.property = property;
      }
      
      /**
       * @return the property
       */
      public String getProperty()
      {
         return property;
      }
   }
}
