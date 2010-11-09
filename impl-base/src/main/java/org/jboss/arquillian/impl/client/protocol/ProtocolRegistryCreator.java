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
package org.jboss.arquillian.impl.client.protocol;

import java.util.Collection;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.configuration.model.ProtocolImpl;
import org.jboss.arquillian.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.protocol.Protocol;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;

/**
 * Responsible for creating and filling the ProtocolRegistry.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ProtocolRegistryCreator
{
   @Inject 
   private Instance<ServiceLoader> serviceLoader;
   
   @Inject @ApplicationScoped
   private InstanceProducer<ProtocolRegistry> registryInstance;
   
   public void createRegistry(@Observes ArquillianDescriptor event) throws Exception
   {
      Collection<Protocol> protocols = serviceLoader.get().all(Protocol.class); 

      Protocol defaultProtocol = null;
      ProtocolImpl protocolImpl = event.getSchemaModel().getProtocol();
      if(protocolImpl != null)
      {
         defaultProtocol = findMatch(new ProtocolDescription(protocolImpl.getType()), protocols);
         if(defaultProtocol == null)
         {
            // TODO: add printout of found protocols
            throw new IllegalStateException("Defined default protocol " + protocolImpl.getType() + " can not be found on classpath");
         }
      }
      ProtocolRegistry registry = new ProtocolRegistry();
      for(Protocol protocol : protocols)
      {
         if(defaultProtocol != null && protocol.equals(defaultProtocol))
         {
            registry.addProtocol(new ProtocolDefinition(protocol, protocolImpl, true));   
         }
         else
         {
            registry.addProtocol(new ProtocolDefinition(protocol));            
         }
      }
      registryInstance.set(registry);
   }
   
   private Protocol<?> findMatch(ProtocolDescription description, Collection<Protocol> protocols)
   {
      for(Protocol<?> protocol : protocols)
      {
         if(description.equals(protocol.getDescription()))
         {
            return protocol;
         }
      }
      return null;
   }
}
