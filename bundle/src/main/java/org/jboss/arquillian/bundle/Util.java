/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.bundle;

// $Id$

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * Loads the configured test {@link Bridge} instance.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2009
 */
public abstract class Util
{
   // Load an instance for a given class name.
   // Use the SomeObject(Properties) ctor if present.
   public static Object loadInstance(String className, Properties props)
   {
      // net bridge class
      Class<?> instanceClass = loadClass(className);

      // get instance with properties
      Object instance = null;
      try
      {
         Constructor<?> ctor = instanceClass.getConstructor(Properties.class);
         instance = ctor.newInstance(props);
      }
      catch (NoSuchMethodException ex)
      {
         // ignore
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot load: " + className, ex);
      }

      // get instance with default ctor
      if (instance == null)
      {
         try
         {
            instance = instanceClass.newInstance();
         }
         catch (Exception ex)
         {
            throw new IllegalStateException("Cannot load: " + className, ex);
         }
      }

      return instance;
   }

   // Load a given class name.
   public static Class<?> loadClass(String className)
   {
      // net bridge class
      Class<?> instanceClass;
      try
      {
         instanceClass = Class.forName(className);
      }
      catch (ClassNotFoundException ex)
      {
         throw new IllegalStateException("Cannot load: " + className, ex);
      }

      return instanceClass;
   }
   
   public static void copyStream(InputStream in, OutputStream out) throws IOException
   {
      byte[] bytes = new byte[1024];
      int read = in.read(bytes);
      while (read > 0)
      {
         out.write(bytes, 0, read);
         read = in.read(bytes);
      }
   }
}
