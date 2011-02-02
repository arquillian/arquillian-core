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
package org.jboss.arquillian.impl.client.container;

import java.util.Arrays;

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
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
public class DeploymentExceptionHandlerTestCase extends AbstractManagerTestBase
{
   @Inject @ApplicationScoped
   private InstanceProducer<ServiceLoader> serviceProducer;
   
   @Mock
   private ServiceLoader serviceLoader;

   @Mock
   private DeploymentExceptionTransformer transformer;

   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(DeploymentExceptionHandler.class);
   }
   
   @Before
   public void registerServiceLoader()
   {
      serviceProducer.set(serviceLoader);
   }
   
   @Test
   public void shouldSwallowExceptionIfExpected() throws Exception
   {
      bind(
            ApplicationScoped.class, 
            DeploymentDescription.class, 
            new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class))
               .setExpectedException(NullPointerException.class));
      
      fire(new DeploymentException("Could not handle ba", new NullPointerException()));
   }

   @Test
   public void shouldCallDeploymentTransformers() throws Exception
   {
      bind(
            ApplicationScoped.class, 
            DeploymentDescription.class, 
            new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class))
               .setExpectedException(IllegalArgumentException.class));

      Mockito.when(serviceLoader.all(DeploymentExceptionTransformer.class)).thenReturn(Arrays.asList(transformer));

      fire(new IllegalArgumentException());
      
      /*
       *  TODO: we check if it's been called twice because the EventRegistry Observer observes Object and throws any Exceptions.
       *  So the first time it gets invoked it is because this is thrown, then since it's handled
       *  it will call next listener which is the DeploymentExceptionHandler.
       */
      Mockito.verify(transformer, Mockito.times(2)).transform(Mockito.isA(Exception.class));
   }
   
   @Test(expected = DeploymentException.class)
   public void shouldRethrowExceptionIfWrongExpectedType() throws Exception
   {
      bind(
            ApplicationScoped.class, 
            DeploymentDescription.class, 
            new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class))
               .setExpectedException(IllegalArgumentException.class));
      
      fire(new DeploymentException("Could not handle ba", new NullPointerException()));
   }

   @Test(expected = DeploymentException.class)
   public void shouldRethrowExceptionIfExpectedNotSet() throws Exception
   {
      bind(
            ApplicationScoped.class, 
            DeploymentDescription.class, 
            new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class)));
      
      fire(new DeploymentException("Could not handle ba", new NullPointerException()));
   }

   @Test(expected = NullPointerException.class)
   public void shouldRethrowExceptionIfNotNoDeploymentFound() throws Exception
   {
      fire(new NullPointerException());
   }
}
