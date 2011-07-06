/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.test.impl;

import java.lang.reflect.Method;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.api.RunAsClient;

/**
 * RunModeUtils
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class RunModeUtils
{
   private RunModeUtils() { }
   
   /**
    * Check is this should run as client.
    * 
    * Verify @Deployment.testable vs @RunAsClient on Class or Method level 
    * 
    * @param description
    * @param testClass
    * @param testMethod
    * @return
    */
   public static boolean isRunAsClient(DeploymentDescription description, Class<?> testClass, Method testMethod)
   {
      boolean runAsClient = true;
      if(description != null)
      {
         runAsClient =  description.testable() ? false:true;
         
         if(testMethod.isAnnotationPresent(RunAsClient.class))
         {
            runAsClient = true;
         }
         else if(testClass.isAnnotationPresent(RunAsClient.class))
         {
            runAsClient = true;
         }
      }
      return runAsClient;
   }
}
