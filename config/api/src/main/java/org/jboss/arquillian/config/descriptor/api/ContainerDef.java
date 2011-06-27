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
package org.jboss.arquillian.config.descriptor.api;

import java.util.List;
import java.util.Map;

/**
 * ContainerDescription
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface ContainerDef extends ArquillianDescriptor
{
   String getContainerName();
   
   ContainerDef setContainerName(String name);
   
   ContainerDef setDefault();
   
   boolean isDefault();
   
   ContainerDef setMode(String mode);
   
   String getMode();
   
   ContainerDef dependency(String artifactId);
   
   ContainerDef property(String name, String value);
   
   ContainerDef overrideProperty(String name, String value);
   
   Map<String, String> getContainerProperties();
   
   ProtocolDef protocol(String type);
   
   List<ProtocolDef> getProtocols();
   
   List<String> getDependencies();
}
