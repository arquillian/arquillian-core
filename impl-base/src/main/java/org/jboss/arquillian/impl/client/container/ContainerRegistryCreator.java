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

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.configuration.model.ArquillianModel;
import org.jboss.arquillian.impl.configuration.model.ContainerImpl;
import org.jboss.arquillian.impl.configuration.model.GroupImpl;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;

/**
 * ContainerRegistryCreator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerRegistryCreator
{
   @Inject @ApplicationScoped
   private InstanceProducer<ContainerRegistry> registry;
   
   @Inject
   private Instance<ServiceLoader> loader;

   public void createRegistry(@Observes ArquillianDescriptor event)
   {
      ContainerRegistry reg = new ContainerRegistry();
      ServiceLoader serviceLoader = loader.get();
      
      ArquillianModel model = event.getSchemaModel();

      // TODO: findActivatedGroupOrContainer
      for(GroupImpl group : model.getGroups())
      {
         for(ContainerImpl container : group.getContainers())
         {
            reg.create(container, serviceLoader);
         }
      }

      // export
      registry.set(reg);
   }
}
