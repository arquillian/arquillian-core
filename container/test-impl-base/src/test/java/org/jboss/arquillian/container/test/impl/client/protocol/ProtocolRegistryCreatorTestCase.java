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
package org.jboss.arquillian.container.test.impl.client.protocol;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.impl.client.protocol.ProtocolRegistryCreator;
import org.jboss.arquillian.container.test.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.container.test.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * ProtocolRegistryCreatorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolRegistryCreatorTestCase extends AbstractContainerTestTestBase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock
   private Protocol<?> protocol;

   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(ProtocolRegistryCreator.class);
   }
   
   @Before
   public void setup() throws Exception
   {
      bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
   }
   
   
   @Test
   public void shouldBindProtocolRegistryToContext() throws Exception
   {
      fire(createDescriptor());
      
      assertEventFiredInContext(ProtocolRegistry.class, ApplicationContext.class);
   }
   
   @SuppressWarnings("rawtypes")
   @Test
   public void shouldBindFoundProtocolsToRegistry() throws Exception
   {
      String protocolName = "protocol";
      Mockito.when(protocol.getDescription()).thenReturn(new ProtocolDescription(protocolName));
      Mockito.when(serviceLoader.all(Protocol.class)).thenReturn(Arrays.asList((Protocol)protocol));
      
      fire(createDescriptor());
      
      ProtocolRegistry registry = getManager().resolve(ProtocolRegistry.class);
      ProtocolDefinition registeredProtocol = registry.getProtocol(new ProtocolDescription(protocolName));
      Assert.assertNotNull(
            "Verify " + Protocol.class.getSimpleName() + " was registered",
            registeredProtocol);
      
      Assert.assertEquals(
            "Verify same protocol instance was registered", 
            protocol, registeredProtocol.getProtocol());
   }
   
   private ArquillianDescriptor createDescriptor()
   {
      return Descriptors.create(ArquillianDescriptor.class);
   }
}
