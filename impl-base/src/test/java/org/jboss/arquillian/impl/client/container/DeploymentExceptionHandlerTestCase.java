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

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;


/**
 * DeploymentExceptionHandlerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentExceptionHandlerTestCase extends AbstractManagerTestBase
{
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(DeploymentExceptionHandler.class);
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
