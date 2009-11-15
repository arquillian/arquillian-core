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
package org.jboss.arquillian.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.arquillian.api.TestMethodExecutor;
import org.jboss.arquillian.api.TestResult;

/**
 * ServletMethodExecutor
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletMethodExecutor implements TestMethodExecutor
{
   private TestMethodExecutor originalExecutor;
   
   public ServletMethodExecutor(TestMethodExecutor originalExecutor)
   {
      this.originalExecutor = originalExecutor;
   }
   
   @Override
   public Object getInstance()
   {
      return originalExecutor.getInstance();
   }
   
   @Override
   public Method getMethod()
   {
      return originalExecutor.getMethod();
   }
   
   @Override
   public void invoke() throws Throwable
   {
      Class<?> testClass = getInstance().getClass();
      String url = "http://localhost:8080/test/?outputMode=serializedObject&className=" + testClass.getName() + "&methodName=" + getMethod().getName();
      long timeoutTime = System.currentTimeMillis() + 1000;
      boolean interrupted = false;
      while (timeoutTime > System.currentTimeMillis())
      {
         URLConnection connection = new URL(url).openConnection();
         if (!(connection instanceof HttpURLConnection))
         {
            throw new IllegalStateException("Not an http connection! " + connection);
         }
         HttpURLConnection httpConnection = (HttpURLConnection) connection;
         httpConnection.setUseCaches(false);
         httpConnection.setDefaultUseCaches(false);
         try
         {
            httpConnection.connect();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
               ObjectInputStream ois = new ObjectInputStream(httpConnection.getInputStream());
               Object o;
               try
               {
                  o = ois.readObject();
               }
               catch (ClassNotFoundException e)
               {
                  IOException ioException = new IOException();
                  ioException.initCause(e);
                  throw ioException;
               }
               ois.close();
               if (!(o instanceof TestResult))
               {
                  throw new IllegalStateException("Error reading test results - expected a TestResult but got " + o);
               }
               TestResult result = (TestResult) o;
               
               if(result.getThrowable() != null) {
                  throw result.getThrowable();
               }
               return;
            }
            else if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND)
            {
               throw new IllegalStateException("Error launching test " + testClass.getName() + " at " + url + ". Got " + httpConnection.getResponseCode() + " ("+ httpConnection.getResponseMessage() + ")");
            }
            try
            {
               Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
               interrupted = true;
            }
         }
         finally
         {
            httpConnection.disconnect();
         }
      }
      if (interrupted)
      {
         Thread.currentThread().interrupt();
      }
      throw new IllegalStateException("Error launching test " + testClass.getName() + " at " + url + ". Kept on getting 404s.");
   }
   
}
