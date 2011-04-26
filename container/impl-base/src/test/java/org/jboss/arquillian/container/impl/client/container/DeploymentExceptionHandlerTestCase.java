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
package org.jboss.arquillian.container.impl.client.container;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.impl.client.container.DeploymentExceptionHandler;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * DeploymentExceptionHandlerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class DeploymentExceptionHandlerTestCase extends AbstractContainerTestBase
{
   @Inject @ApplicationScoped
   private InstanceProducer<ServiceLoader> serviceProducer;
   
   @Mock
   private ServiceLoader serviceLoader;

   @Mock
   private DeploymentExceptionTransformer transformer;

   @Mock
   private Container container;

   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(DeploymentExceptionHandler.class);
      extensions.add(TestExceptionDeployThrower.class);
   }
   
   @Before
   public void registerServiceLoader()
   {
      serviceProducer.set(serviceLoader);
   }
   
   @Test
   public void shouldSwallowExceptionIfExpected() throws Exception
   {
      TestExceptionDeployThrower.shouldThrow = new DeploymentException("Could not handle ba", new NullPointerException()); 
      fire(new DeployDeployment(
            container, 
            new Deployment(new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class))
               .setExpectedException(NullPointerException.class))));
   }

   @Test
   public void shouldCallDeploymentTransformers() throws Exception
   {
      TestExceptionDeployThrower.shouldThrow = new DeploymentException("Could not handle ba", new IllegalArgumentException()); 
      Mockito.when(serviceLoader.all(DeploymentExceptionTransformer.class)).thenReturn(Arrays.asList(transformer));

      fire(new DeployDeployment(
            container, 
            new Deployment(new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class))
            .setExpectedException(IllegalArgumentException.class))));
      
      Mockito.verify(transformer, Mockito.times(1)).transform(Mockito.isA(Exception.class));
   }

   @Test
   public void shouldTransformException() throws Exception
   {
      TestExceptionDeployThrower.shouldThrow = new IllegalStateException();
      Mockito.when(serviceLoader.all(DeploymentExceptionTransformer.class)).thenReturn(Arrays.asList(transformer));
      Mockito.when(transformer.transform(TestExceptionDeployThrower.shouldThrow)).thenReturn(new IllegalArgumentException());

      fire(new DeployDeployment(
            container, 
            new Deployment(new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class))
               .setExpectedException(IllegalArgumentException.class))));
   }

   @Test(expected = DeploymentException.class)
   public void shouldRethrowExceptionIfWrongExpectedType() throws Exception
   {
      TestExceptionDeployThrower.shouldThrow = new DeploymentException("Could not handle ba", new NullPointerException()); 
      Mockito.when(serviceLoader.all(DeploymentExceptionTransformer.class)).thenReturn(Arrays.asList(transformer));

      fire(new DeployDeployment(
            container, 
            new Deployment(new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class))
               .setExpectedException(IllegalArgumentException.class))));
      }

   @Test(expected = DeploymentException.class)
   public void shouldRethrowExceptionIfExpectedNotSet() throws Exception
   {
      TestExceptionDeployThrower.shouldThrow = new DeploymentException("Could not handle ba", new NullPointerException()); 

      fire(new DeployDeployment(
            container, 
            new Deployment(new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class)))));
   }

   @Test(expected = RuntimeException.class)
   public void shouldThrowExceptionIfExpectedButNoExceptionThrown() throws Exception
   {
      TestExceptionDeployThrower.shouldThrow = null;

      fire(new DeployDeployment(
            container, 
            new Deployment(new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class))
               .setExpectedException(IllegalArgumentException.class))));
   }

   public static class TestExceptionDeployThrower 
   {
      public static Throwable shouldThrow = null;
      
      public void throwException(@Observes DeployDeployment event) throws Throwable
      {
         if(shouldThrow != null)
         {
            throw shouldThrow;
         }
      }
   }
}
