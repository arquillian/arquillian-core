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

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;


/**
 * DeploymentAnnotationArchiveGeneratorTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentAnnotationArchiveGeneratorTestCase
{
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentNotPresent() throws Exception
   {
      new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(DeploymentNotPresent.class);
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentNotStatic() throws Exception
   {
      new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(DeploymentNotStatic.class);
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentWrongReturnType() throws Exception
   {
      new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(DeploymentWrongReturnType.class);
   }
   
   @Test
   public void shouldThrowExceptionOnDeploymentOk() throws Exception
   {
      new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(DeploymentOK.class);
   }

   private static class DeploymentNotPresent {}
   
   private static class DeploymentNotStatic 
   {
      @SuppressWarnings("unused")
      @Deployment
      public Archive<?> test() {
         return ShrinkWrap.create("test.jar", JavaArchive.class);
      }
   }
   
   private static class DeploymentWrongReturnType
   {
      @SuppressWarnings("unused")
      @Deployment
      public Object test() {
         return ShrinkWrap.create("test.jar", JavaArchive.class);
      }
   }
   
   private static class DeploymentOK
   {
      @SuppressWarnings("unused")
      @Deployment
      public static JavaArchive test() {
         return ShrinkWrap.create("test.jar", JavaArchive.class);
      }
   }   
}
