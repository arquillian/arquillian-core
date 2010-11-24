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

import java.net.URL;
import java.net.URLClassLoader;

/**
 * FilteredURLClassLoader
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class FilteredURLClassLoader extends URLClassLoader
{

   private String regExpFilter;
   
   public FilteredURLClassLoader(URL[] urls, String regExpFilter)
   {
      super(urls);
      this.regExpFilter = regExpFilter;
   }

   public java.lang.Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      if (!name.matches(regExpFilter))
      {
         Class<?> c = findLoadedClass(name);
         if (c == null)
         {
            URL resource = super.findResource(name.replace('.', '/').concat(".class"));
            if (resource != null)
            {
               c = super.findClass(name);
            }
         }
         if (c != null) // class found locally
         {
            return c;
         }
      }
      try
      {
         return getParent().loadClass(name);
      }
      catch (ClassNotFoundException e)
      {
         return super.loadClass(name, resolve);
      }
   }

   public java.net.URL getResource(String name)
   {
      java.net.URL url = null;
      if (!name.matches(regExpFilter))
      {
         url = findResource(name);
         if (url == null)
         {
            url = super.findResource(name);
         }
         if (url != null)
         {
            return url;
         }
      }
      url = getParent().getResource(name);
      if (url == null)
      {
         return super.getResource(name);
      }
      return url;
   }
}
