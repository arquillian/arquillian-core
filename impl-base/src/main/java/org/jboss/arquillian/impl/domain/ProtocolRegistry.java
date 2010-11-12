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

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.spi.client.protocol.Protocol;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;

/**
 * A registry holding all found {@link Protocol}s.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ProtocolRegistry
{
   private List<ProtocolDefinition> protocols;
   
   public ProtocolRegistry()
   {
      protocols = new ArrayList<ProtocolDefinition>();
   }

   /**
    * @param protocol The Protocol to add
    * @return this
    * @throws IllegalArgumentException if a protocol with same name found
    */
   public ProtocolRegistry addProtocol(ProtocolDefinition protocolDefinition)
   {
      Validate.notNull(protocolDefinition, "ProtocolDefinition must be specified");
      ProtocolDefinition protocolAllReadyRegistered = findSpecificProtocol(protocolDefinition.getProtocol().getDescription());
      if(protocolAllReadyRegistered != null)
      {
         throw new IllegalArgumentException(
               "Protocol with description " + protocolDefinition.getProtocol().getDescription() + " allready registered. " +
                "Registered " + protocolAllReadyRegistered.getClass() + ", trying to register " + protocolDefinition.getProtocol().getClass());
      }
      protocols.add(protocolDefinition);
      return this;
   }

   /**
    * @param protocolDescription
    * @return
    */
   public ProtocolDefinition getProtocol(ProtocolDescription protocolDescription)
   {
      Validate.notNull(protocolDescription, "ProtocolDescription must be specified");
      if(ProtocolDescription.DEFAULT.equals(protocolDescription))
      {
         return findDefaultProtocol();
      }
      return findSpecificProtocol(protocolDescription);
   }
   
   /**
    * @return
    */
   private ProtocolDefinition findDefaultProtocol()
   {
      for(ProtocolDefinition def : protocols)
      {
         if(def.isDefaultProtocol())
         {
            return def;
         }
      }
      if(protocols.size() == 1)
      {
         return protocols.get(0);
      }
      return null;
   }

   /**
    * @return
    */
   private ProtocolDefinition findSpecificProtocol(ProtocolDescription protocolDescription)
   {
      for(ProtocolDefinition protocol : protocols)
      {
         if(protocolDescription.equals(protocol.getProtocol().getDescription()))
         {
            return protocol;
         }
      }
      return null;
   }
}
