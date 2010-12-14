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

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.impl.MapObject;
import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.spi.client.protocol.Protocol;
import org.jboss.arquillian.spi.client.protocol.ProtocolConfiguration;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;


/**
 * ProtocolDefinition
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ProtocolDefinition
{
   private Protocol<?> protocol;
   private Map<String, String> protocolConfiguration;
   private boolean defaultProtocol = false;
   
   public ProtocolDefinition(Protocol<?> protocol)
   {
      this(protocol, new HashMap<String, String>(), false);
   }

   public ProtocolDefinition(Protocol<?> protocol, Map<String, String> protocolConfiguration)
   {
      this(protocol, protocolConfiguration, false);
   }
   
   public ProtocolDefinition(Protocol<?> protocol, Map<String, String> protocolConfiguration, boolean defaultProtocol)
   {
      Validate.notNull(protocol, "Protocol must be specified");
      Validate.notNull(protocolConfiguration, "ProtocolConfiguration must be specified");
      Validate.notNull(defaultProtocol, "DefaultProtocol must be specified");
      
      this.protocol = protocol;
      this.protocolConfiguration = protocolConfiguration;
      this.defaultProtocol = defaultProtocol;
   }
   
   public ProtocolDescription getProtocolDescription()
   {
      return protocol.getDescription();
   }
   
   /**
    * @return the defaultProtocol
    */
   public boolean isDefaultProtocol()
   {
      return defaultProtocol;
   }
   
   /**
    * @return the protocol
    */
   public Protocol<?> getProtocol()
   {
      return protocol;
   }
   
   /**
    * @return the name
    */
   public String getName()
   {
      return protocol.getDescription().getName();
   }
   
   /**
    * @return the protocolConfiguration
    */
   public Map<String, String> getProtocolConfiguration()
   {
      return protocolConfiguration;
   }

   /**
    * Create a new ProtocolConfiguration based on default configuration.
    * 
    * @return
    * @throws Exception
    */
   public ProtocolConfiguration createProtocolConfiguration() throws Exception
   {
      return createProtocolConfiguration(protocolConfiguration);
   }

   /**
    * Create a new ProtocolConfiguration based on given configuration.
    * 
    * @return
    * @throws Exception
    */
   public ProtocolConfiguration createProtocolConfiguration(Map<String, String> configuration) throws Exception
   {
      Validate.notNull(configuration, "ProtocolConfiguration must be specified");
      ProtocolConfiguration config = protocol.getProtocolConfigurationClass().newInstance();
      MapObject.populate(config, configuration);
      return config;
   }
}
