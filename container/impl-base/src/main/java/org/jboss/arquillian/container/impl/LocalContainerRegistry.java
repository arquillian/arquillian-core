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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.Validate;

/**
 * ContainerManager
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LocalContainerRegistry implements ContainerRegistry
{
   private List<Container> containers;
   
   private Injector injector;

   public LocalContainerRegistry(Injector injector)
   {
      this.containers = new ArrayList<Container>();
      this.injector = injector;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.domain.ContainerRegistryA#create(org.jboss.arquillian.impl.configuration.api.ContainerDef, org.jboss.arquillian.core.spi.ServiceLoader)
    */
   @Override
   public Container create(ContainerDef definition, ServiceLoader loader)
   {
      Validate.notNull(definition, "Definition must be specified");

      try
      {
         // TODO: this whole Classloading thing is a HACK and does not work. Need to split out into multiple JVMs for multi container testing
//         ClassLoader containerClassLoader;
//         if(definition.getDependencies().size() > 0)
//         {
//            final MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class).artifacts(
//                  definition.getDependencies().toArray(new String[0]));
//            
//            URL[] resolvedURLs = MapObject.convert(resolver.resolveAsFiles());
//
//            containerClassLoader = new FilteredURLClassLoader(resolvedURLs, "org.jboss.(arquillian|shrinkwrap)..*");
//         }
//         else
//         {
//            containerClassLoader = LocalContainerRegistry.class.getClassLoader();
//         }
//            
         return addContainer(
               //before a Container is added to a collection of containers, inject into its injection point
               injector.inject(new ContainerImpl(
                               definition.getContainerName(), 
                               loader.onlyOne(DeployableContainer.class),
                               definition)));
      }
      catch (Exception e) 
      {
         throw new ContainerCreationException("Could not create Container " + definition.getContainerName(), e);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.domain.ContainerRegistryA#getContainer(java.lang.String)
    */
   @Override
   public Container getContainer(String name)
   {
      return findMatchingContainer(name);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.domain.ContainerRegistryA#getContainers()
    */
   @Override
   public List<Container> getContainers()
   {
      return Collections.unmodifiableList(containers);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.domain.ContainerRegistryA#getContainer(org.jboss.arquillian.spi.client.deployment.TargetDescription)
    */
   @Override
   public Container getContainer(TargetDescription target)
   {
      Validate.notNull(target, "Target must be specified");
      if(TargetDescription.DEFAULT.equals(target))
      {
         return findDefaultContainer();
      }
      return findMatchingContainer(target.getName());
   }

   private Container addContainer(Container contianer)
   {
      containers.add(contianer);
      return contianer;
   }
   
   /**
    * @return
    */
   private Container findDefaultContainer()
   {
      if(containers.size() == 1)
      {
         return containers.get(0);
      }
      for(Container container : containers)
      {
        if(container.getContainerConfiguration().isDefault())
        {
           return container;
        }
      }
      return null;
   }

   private Container findMatchingContainer(String name)
   {
      for(Container container: containers)
      {
         if(container.getName().equals(name))
         {
            return container;
         }
      }
      return null;
   }
}