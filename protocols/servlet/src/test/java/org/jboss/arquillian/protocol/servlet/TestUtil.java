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
package org.jboss.arquillian.protocol.servlet;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

/**
 * TestUtil
 * 
 * Internal helper for testcase to do http request
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestUtil
{
   private TestUtil() {}

   public static Object execute(URL url) 
   {
      ObjectInputStream input = null;
      try 
      {
         input = new ObjectInputStream(url.openStream());
         return input.readObject();
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not fetch url " + url);
      }
      finally 
      {
         close(input);
      }
   }
   
   private static void close(InputStream input) 
   {
      if(input == null) 
      {
         return;
      }
      try 
      {
         input.close();
      } 
      catch (Exception e) 
      {
         // ignore
      }
   }
}
