/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.impl.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.jboss.arquillian.spi.DeployableContainer;

/**
 * DeployableContainers
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeployableContainers
{

   private DeployableContainers() {}
   
   public static DeployableContainer load() 
   {
      ServiceLoader<DeployableContainer> containerLoader = ServiceLoader.load(DeployableContainer.class);
      List<DeployableContainer> containers = toList(containerLoader.iterator());
      if(containers.size() == 0)
      {
         throw new RuntimeException("No containers found");
      }
      if(containers.size() > 1)
      {
         throw new RuntimeException("More the one container found, check classpath");
      }
      return containers.get(0);
   }
   
   private static <T> List<T> toList(Iterator<T> iterator) 
   {
      List<T> list = new ArrayList<T>();
      while(iterator.hasNext())
      {
         list.add(iterator.next());
      }
      return list;
   }
}
