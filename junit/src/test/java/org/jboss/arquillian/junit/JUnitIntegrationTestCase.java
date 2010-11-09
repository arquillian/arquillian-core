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
import org.jboss.arquillian.api.DeploymentTarget;
import org.jboss.arquillian.api.Protocol;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;


/**
 * Verify the that JUnit integration adaptor fires the expected events even when Handlers are failing.
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
   public void shouldHandleTheLifecycleCorrectlyOnMultipleTestRunsWithExceptions() throws Throwable 
   {
      JUnitCore runner = new JUnitCore();
      Result result = runner.run(
            Request.classes(ArquillianClass1.class, ArquillianClass1.class));

      Assert.assertEquals(
            "Verify that both exceptions thrown bubbled up",
            2, result.getFailureCount());
      
      // Exceptions returned are wrapped in a InvocationException and InvocatioTargetException, verify the cause 
      Assert.assertEquals(
            "Verify exception thrown",
            "deploy", result.getFailures().get(0).getException().getCause().getCause().getMessage());
      
      Assert.assertEquals(
            "Verify exception thrown",
            "undeploy", result.getFailures().get(1).getException().getCause().getCause().getMessage());
      
      Assert.assertFalse(result.wasSuccessful());
      
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
      
      Assert.assertEquals("Verify test invoked only once, first run should fail during deploy", 
            1, (int)containerCallbacks.get("shouldBeInvoked"));
   }
   
   @RunWith(Arquillian.class)
   public static class ArquillianClass1 
   {
      @Protocol("local")
      @Deployment(name = "test")
      public static JavaArchive create() 
      {
         return ShrinkWrap.create(JavaArchive.class, "test.jar");
      }
      
      @DeploymentTarget("test")
      @Test
      public void shouldBeInvoked() throws Exception 
      {
         JUnitIntegrationTestCase.wasCalled("shouldBeInvoked");
      }
   }
}
