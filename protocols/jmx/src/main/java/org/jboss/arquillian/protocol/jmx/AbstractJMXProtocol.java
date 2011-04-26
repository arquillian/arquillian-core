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
package org.jboss.arquillian.protocol.jmx;

import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.spi.client.protocol.Protocol;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;

/**
 * AbstractJMXProtocol
 *
 * @author thomas.diesler@jboss.com
 * @since 21-Apr-2011
 */
public abstract class AbstractJMXProtocol implements Protocol<JMXProtocolConfiguration>
{
   @Override
   public Class<JMXProtocolConfiguration> getProtocolConfigurationClass()
   {
      return JMXProtocolConfiguration.class;
   }

   @Override
   public ProtocolDescription getDescription()
   {
      return new ProtocolDescription(getProtocolName());
   }

   @Override
   public ContainerMethodExecutor getExecutor(JMXProtocolConfiguration protocolConfiguration, ProtocolMetaData metaData)
   {
      return new JMXMethodExecutor();
   }

   @Override
   public abstract DeploymentPackager getPackager();

   public abstract String getProtocolName();
}
