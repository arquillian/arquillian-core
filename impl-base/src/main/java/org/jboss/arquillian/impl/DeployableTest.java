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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.naming.InitialContext;

import org.jboss.arquillian.api.ArchiveGenerator;
import org.jboss.arquillian.api.Controlable;
import org.jboss.arquillian.api.Deployer;
import org.jboss.arquillian.api.TestMethodExecutor;
import org.jboss.shrinkwrap.api.Archive;

/**
 * DeployableTest
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeployableTest
{
   private static boolean inContainer = false;
   
   public static boolean isInContainer()
   {
      return inContainer;
   }

   public static void setInContainer(boolean inContainer)
   {
      DeployableTest.inContainer = inContainer;
   }

   private Controlable containerController;
   private Deployer containerDeployer;
   
   public DeployableTest(Controlable containerController, Deployer containerDeployer)
   {
      this.containerController = containerController;
      this.containerDeployer = containerDeployer;
   }
   
   public Controlable getContainerController() 
   {
      return containerController;
   }

   public Deployer getDeployer() 
   {
      return containerDeployer;
   }

   
   public ArchiveGenerator getArchiveGenerator() 
   {
      if(DeployableTest.isInContainer()) 
      {
         return new NullArtifactGenerator();
      }
      return new UserCreatedArchiveGenerator();
   }

   public Archive<?> generateArchive(Class<?> testCase) 
   {
      return getArchiveGenerator().generateArchive(testCase);
   }


   public void run(TestMethodExecutor executor) throws Throwable 
   {
      if(inContainer) 
      {
         injectClass(executor.getInstance());
         executor.invoke();
      } 
      else 
      {
         new ServletMethodExecutor(executor).invoke();
      }
   }
   
   void injectClass(Object testCase) 
   {
      try 
      {
         Class<? extends Annotation> ejbAnnotationClass = (Class<? extends Annotation>)Thread.currentThread()
                     .getContextClassLoader().loadClass("javax.ejb.EJB");
         
         for(Field field : testCase.getClass().getDeclaredFields()) 
         {
            if(field.isAnnotationPresent(ejbAnnotationClass)) 
            {
               Object ejb = lookupEJB(field);
               field.setAccessible(true);
               field.set(testCase, ejb);
            }
         }
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not inject members", e);
      }
   }
   
   private Object lookupEJB(Field field) throws Exception 
   {
      InitialContext context = new InitialContext();
      return context.lookup("test/" + field.getType().getSimpleName() + "Bean/local");
   }
   
   void invokeMethod(Method testMethod, Class<?> testCase) 
   {
   }

}
