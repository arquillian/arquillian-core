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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * MapObjectPopulator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class MapObject
{
   public static Logger log = Logger.getLogger(MapObject.class.getName());

   public static void populate(Object object, Map<String, String> values) throws Exception
   {
      Map<String, String> clonedValues = new HashMap<String, String>(values);
      Set<String> candidates = new HashSet<String>();
      for (Method candidate : object.getClass().getMethods())
      {
         String methodName = candidate.getName();
         if (methodName.matches("^set[A-Z].*") &&
               candidate.getReturnType().equals(Void.TYPE) &&
               candidate.getParameterTypes().length == 1)
         {
            candidate.setAccessible(true);
            String propertyName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
            candidates.add(propertyName);
            if (clonedValues.containsKey(propertyName))
            {
               candidate.invoke(
                     object, 
                     convert(candidate.getParameterTypes()[0], clonedValues.get(propertyName)));
               clonedValues.remove(propertyName);
            }
         }
      }
      if(clonedValues.size() > 0) 
      {
         log.warning(
               "Configuration contain properties not supported by the backing object " + object.getClass().getName() + "\n" +
               "Unused property entries: " + clonedValues + "\n" +
               "Supported property names: " + candidates);
      }
   }

   public static URL[] convert(File[] files)
   {
      URL[] urls = new URL[files.length];
      try
      {
         for(int i = 0 ; i < files.length; i++)
         {
            urls[i] = files[i].toURI().toURL();
            //System.out.println(urls[i]);
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not create URL from a File object?", e);
      }
      return urls;
   }
   
   /**
    * Converts a String value to the specified class.
    * @param clazz
    * @param value
    * @return
    */
   private static Object convert(Class<?> clazz, String value) 
   {
      /* TODO create a new Converter class and move this method there for reuse */
      
      if (Integer.class.equals(clazz) || int.class.equals(clazz)) 
      {
         return Integer.valueOf(value);
      } 
      else if (Double.class.equals(clazz) || double.class.equals(clazz)) 
      {
         return Double.valueOf(value);
      } 
      else if (Long.class.equals(clazz) || long.class.equals(clazz))
      {
         return Long.valueOf(value);
      }
      else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz))
      {
         return Boolean.valueOf(value);
      }
      
      return value;
   }
}
