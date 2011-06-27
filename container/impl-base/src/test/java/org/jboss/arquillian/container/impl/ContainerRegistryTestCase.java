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
package org.jboss.arquillian.container.impl;

import junit.framework.Assert;

import org.jboss.arquillian.config.descriptor.impl.ContainerDefImpl;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
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
public class ContainerRegistryTestCase extends AbstractContainerTestBase
{
   private static final String ARQUILLIAN_XML = "arquillian.xml";
   
   @Inject
   private Instance<Injector> injector;
   
   @Mock
   private ServiceLoader serviceLoader;

   @Mock
   private DeployableContainer<DummyContainerConfiguration> deployableContainer;

   @Before
   public void setup() throws Exception
   {
      Mockito.when(serviceLoader.onlyOne(Mockito.same(DeployableContainer.class))).thenReturn(deployableContainer);
      Mockito.when(deployableContainer.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
   }
   
   @Test
   public void shouldBeAbleToDefaultTargetToOnlyRegisteredContainer() throws Exception
   {
      String name = "some-name";
      
      ContainerRegistry registry = new LocalContainerRegistry(injector.get());
      registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name), serviceLoader);
      
      Container container = registry.getContainer(TargetDescription.DEFAULT);
      
      Assert.assertEquals(
            "Verify that the only registered container is returned as default",
            name, container.getName());
   }

   @Test
   public void shouldBeAbleToDefaultTargetToDefaultRegisteredContainer() throws Exception
   {
      String name = "some-name";
      
      ContainerRegistry registry = new LocalContainerRegistry(injector.get());
      registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName("some-other-name"), serviceLoader);
      registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name).setDefault(), serviceLoader);
      
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
      
      ContainerRegistry registry = new LocalContainerRegistry(injector.get());
      registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name)
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
      
      ContainerRegistry registry = new LocalContainerRegistry(injector.get());
      registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName("other-name"), serviceLoader);
      registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name), serviceLoader);
      
      Container container = registry.getContainer(new TargetDescription(name));
      
      Assert.assertEquals(
            "Verify that the specific registered container is returned",
            name, container.getName());
   }
   
   @Test
   public void shouldBeAbleToGetContainerByName() throws Exception
   {
      String name = "some-name";

      ContainerRegistry registry = new LocalContainerRegistry(injector.get());
      registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName("other-name"), serviceLoader);
      registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name), serviceLoader);
      
      Container container = registry.getContainer(name);

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

      /* (non-Javadoc)
       * @see org.jboss.arquillian.spi.client.container.ContainerConfiguration#validate()
       */
      @Override
      public void validate() throws ConfigurationException
      {
         // TODO Auto-generated method stub
         
      }
   }
}
