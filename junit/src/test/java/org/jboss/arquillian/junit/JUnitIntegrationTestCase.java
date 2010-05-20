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
package org.jboss.arquillian.junit;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;


/**
 * Verify the that JUnit integration adaptor fires the expected events.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JUnitIntegrationTestCase
{
   public static Map<String, Integer> containerCallbacks = new HashMap<String, Integer>();
   static 
   {
      containerCallbacks.put("setup", 0);
      containerCallbacks.put("start", 0);
      containerCallbacks.put("stop", 0);
      containerCallbacks.put("deploy", 0);
      containerCallbacks.put("undeploy", 0);
      containerCallbacks.put("shouldBeInvoked", 0);
   }
   
   public static void wasCalled(String name) 
   {
      if(containerCallbacks.containsKey(name))
      {
         containerCallbacks.put(name, containerCallbacks.get(name) + 1);
      }
      else 
      {
         throw new RuntimeException("Unknown callback: " + name);
      }
   }
   
   @Test
   public void shouldHandleTheLifecycleCorrectlyOnMultipleTestRuns() throws Throwable 
   {
      JUnitCore runner = new JUnitCore();
      Result result = runner.run(
            Request.classes(TestClass1.class, TestClass1.class));

      if(result.getFailures().size() > 0)
      {
         throw result.getFailures().get(0).getException();
      }
      
      Assert.assertTrue(result.wasSuccessful());
      
      assertCallbacks();
   }
   
   private void assertCallbacks() throws Exception
   {
      Assert.assertEquals("Verify container only setup once", 
            1, (int)containerCallbacks.get("setup"));

      Assert.assertEquals("Verify container only started once", 
            1, (int)containerCallbacks.get("start"));

      Assert.assertEquals("Verify container only stopped once", 
            1, (int)containerCallbacks.get("stop"));

      Assert.assertEquals("Verify deployed twice", 
            2, (int)containerCallbacks.get("deploy"));

      Assert.assertEquals("Verify undeployed twice", 
            2, (int)containerCallbacks.get("undeploy"));
      
      Assert.assertEquals("Verify test invoked twice", 
            2, (int)containerCallbacks.get("shouldBeInvoked"));
   }
   
   @RunWith(Arquillian.class)
   public static class TestClass1 
   {
      @Deployment
      public static JavaArchive create() 
      {
         return ShrinkWrap.create("test.jar", JavaArchive.class);
      }
      
      @Test
      public void shouldBeInvoked() 
      {
         JUnitIntegrationTestCase.wasCalled("shouldBeInvoked");
      }
   }
}
