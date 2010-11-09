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
package org.jboss.arquillian.impl.configuration.api;

import org.jboss.arquillian.impl.configuration.model.ArquillianModel;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.descriptor.spi.SchemaDescriptorProvider;

/**
 * ArquillianDescriptor
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface ArquillianDescriptor extends Descriptor, SchemaDescriptorProvider<ArquillianModel>
{
   DefaultProtocolDef defaultProtocol(String type);
   
   ContainerDef container(String name);
   
   GroupDef group(String name);
}
