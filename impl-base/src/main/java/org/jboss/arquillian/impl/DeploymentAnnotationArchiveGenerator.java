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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.spi.ApplicationArchiveGenerator;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ClassContainer;

/**
 * This ApplicationArchiveGenerator implementation will look for a 
 * method with the following signature to generate the deployment:<br/>
 * <br/>
 * <code>
 * @Deployment
 * static Archive<?> x() {}
 * </code> 
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentAnnotationArchiveGenerator implements ApplicationArchiveGenerator 
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ApplicationArchiveGenerator#generateApplicationArchive(org.jboss.arquillian.spi.TestClass)
    */
   /**
    * @throws IllegalAccessException if no method annotated with {@link Deployment} found
    * @throws IllegalAccessException if annotated method is non static
    * @throws IllegalAccessException if annotated methods return type is not Archive<?>
    */
   public Archive<?> generateApplicationArchive(TestClass testCase)
   {
      Validate.notNull(testCase, "TestCase must be specified");
      
      Method deploymentMethod = testCase.getMethod(Deployment.class);
      if(deploymentMethod == null) 
      {
         throw new IllegalArgumentException("No method annotated with " + Deployment.class.getName() + " found");
      }
      if(!Modifier.isStatic(deploymentMethod.getModifiers()))
      {
         throw new IllegalArgumentException("Method annotated with " + Deployment.class.getName() + " is not static");
      }
      if(!Archive.class.isAssignableFrom(deploymentMethod.getReturnType())) 
      {
         throw new IllegalArgumentException("Method annotated with " + Deployment.class.getName() + " must have return type " + Archive.class.getName());
      }
      try 
      {
         Archive<?> archive = (Archive<?>)deploymentMethod.invoke(null);
         // TODO: handle deployment attributes like autoAddPakcage etc..
         
         // TODO: move the RunMode handling to one location
         RunModeType runMode = RunModeType.IN_CONTAINER;
         if(testCase.isAnnotationPresent(Run.class))
         {
            runMode = testCase.getAnnotation(Run.class).value();
         }
         
         try
         {
            if(ClassContainer.class.isInstance(archive) && runMode == RunModeType.IN_CONTAINER) 
            {
               ClassContainer<?> classContainer = ClassContainer.class.cast(archive);
               classContainer.addClass(testCase.getJavaClass());
            }
         } 
         catch (UnsupportedOperationException e) 
         { 
            /*
             * Quick Fix: https://jira.jboss.org/jira/browse/ARQ-118
             * Keep in mind when rewriting for https://jira.jboss.org/jira/browse/ARQ-94
             * that a ShrinkWrap archive might not support a Container if even tho the 
             * ContianerBase implements it. Check the Archive Interface..  
             */
         }
         return archive;
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not get Deployment", e);
      }
   }
}
