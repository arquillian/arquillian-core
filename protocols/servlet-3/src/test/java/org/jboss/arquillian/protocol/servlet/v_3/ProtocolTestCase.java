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
package org.jboss.arquillian.protocol.servlet.v_3;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

import org.jboss.arquillian.protocol.servlet.MockTestRunner;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.protocol.servlet.TestUtil;
import org.jboss.arquillian.protocol.servlet.runner.ServletTestRunner;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;


/**
 * ProtocolTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ProtocolTestCase 
{

   private Server server;
   
   @Before
   public void setup() throws Exception 
   {
      server = new Server(8181);
      Context root = new Context(server, "/arquillian-protocol", Context.SESSIONS);
      root.addServlet(ServletTestRunner.class, "/ArquillianServletRunner");
      server.start();
   }
   
   @After
   public void cleanup() throws Exception
   {
      server.stop();
   }
   
   @Test
   public void shouldReturnTestResult() throws Exception 
   {
      MockTestRunner.add(new TestResult(Status.PASSED, null));
      
      ServletMethodExecutor executor = new ServletMethodExecutor(createBaseURL());
      TestResult result = executor.invoke(new MockTestExecutor());
      
      Assert.assertEquals(
            "Should have returned a passed test",
            MockTestRunner.wantedResults.getStatus(),
            result.getStatus());
      
      Assert.assertNull(
            "No Exception should have been thrown",
            result.getThrowable());
   }
   
   @Test
   public void shouldReturnThrownException() throws Exception 
   {
      MockTestRunner.add(new TestResult(Status.FAILED, new Exception().fillInStackTrace()));
      
      ServletMethodExecutor executor = new ServletMethodExecutor(createBaseURL());
      TestResult result = executor.invoke(new MockTestExecutor());
      
      Assert.assertEquals(
            "Should have returned a passed test",
            MockTestRunner.wantedResults.getStatus(),
            result.getStatus());
      
      Assert.assertNotNull(
            "Exception should have been thrown",
            result.getThrowable());
      
   }
   
   @Test
   public void shouldReturnExceptionWhenMissingTestClassParameter() throws Exception
   {
      URL url = createURL(ServletTestRunner.OUTPUT_MODE_SERIALIZED, null, null);
      TestResult result = (TestResult)TestUtil.execute(url);
      
      Assert.assertEquals(
            "Should have returned a passed test",
            Status.FAILED,
            result.getStatus());
      
      Assert.assertTrue(
            "No Exception should have been thrown",
            result.getThrowable() instanceof IllegalArgumentException);
   }
   
   @Test
   public void shouldReturnExceptionWhenMissingMethodParameter() throws Exception
   {
      URL url = createURL(ServletTestRunner.OUTPUT_MODE_SERIALIZED, "org.my.test", null);
      TestResult result = (TestResult)TestUtil.execute(url);
      
      Assert.assertEquals(
            "Should have returned a passed test",
            Status.FAILED,
            result.getStatus());
      
      Assert.assertTrue(
            "No Exception should have been thrown",
            result.getThrowable() instanceof IllegalArgumentException);
   }
   
   @Test
   public void shouldReturnExceptionWhenErrorLoadingClass() throws Exception
   {
      URL url = createURL(ServletTestRunner.OUTPUT_MODE_SERIALIZED, "org.my.test", "test");
      TestResult result = (TestResult)TestUtil.execute(url);
      
      Assert.assertEquals(
            "Should have returned a passed test",
            Status.FAILED,
            result.getStatus());
      
      Assert.assertTrue(
            "No Exception should have been thrown",
            result.getThrowable() instanceof ClassNotFoundException);
   }

   private URI createBaseURL() {
      return URI.create("http://localhost:" + server.getConnectors()[0].getPort() +  "/arquillian-protocol");
   }
   
   private URL createURL(String outputMode, String testClass, String methodName) 
   {
      StringBuilder url = new StringBuilder(createBaseURL().toASCIIString() + "/ArquillianServletRunner");
      boolean first = true;
      if(outputMode != null) 
      {
         if(first) {first = false; url.append("?"); } else { url.append("&"); }
         url.append(ServletTestRunner.PARA_OUTPUT_MODE).append("=").append(outputMode);
      }
      if(testClass != null) 
      {
         if(first) {first = false; url.append("?"); } else { url.append("&"); }
         url.append(ServletTestRunner.PARA_CLASS_NAME).append("=").append(testClass);
      }
      if(methodName != null) 
      {
         if(first) {first = false; url.append("?"); } else { url.append("&"); }
         url.append(ServletTestRunner.PARA_METHOD_NAME).append("=").append(methodName);
      }
      
      try
      {
         return new URL(url.toString());
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not create url", e);
      }
   }
   
   public static class MockTestExecutor implements TestMethodExecutor, Serializable {
      
      private static final long serialVersionUID = 1L;

      public void invoke() throws Throwable
      {
      }
      
      public Method getMethod()
      {
         try 
         {
            return this.getClass().getMethod("getMethod");               
         }
         catch (Exception e) 
         {
            throw new RuntimeException("Could not find my own method ?? ");
         }
      }
      
      public Object getInstance()
      {
         return this;
      }      
   }
}
