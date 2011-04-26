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
package org.jboss.arquillian.spi.client.protocol.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * ProtocolMetaData
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ProtocolMetaData
{
   private List<Object> contexts = new ArrayList<Object>();
   
   public boolean hasContext(Class<?> clazz)
   {
      for(Object obj: contexts)
      {
         if(clazz.isInstance(obj))
         {
            return true;
         }
      }
      return false;
   }
   
   public <T> T getContext(Class<T> clazz)
   {
      for(Object obj: contexts)
      {
         if(clazz.isInstance(obj))
         {
            return clazz.cast(obj);
         }
      }
      return null;
   }
   
   public ProtocolMetaData addContext(Object obj)
   {
      contexts.add(obj);
      return this;
   }
}
