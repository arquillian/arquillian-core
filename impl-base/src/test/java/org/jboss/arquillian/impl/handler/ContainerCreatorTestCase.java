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
package org.jboss.arquillian.impl.handler;

import junit.framework.Assert;

import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.event.type.SuiteEvent;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.ServiceLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Verify that the {@link DeployableContainer} is setup and exported.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerCreatorTestCase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock
   private DeployableContainer container;
   
   @Test
   public void shouldLoadAndSetupTheContainer() throws Exception
   {
      Mockito.when(serviceLoader.onlyOne(DeployableContainer.class)).thenReturn(container);
      
      Configuration configuration = new Configuration();
      
      SuiteContext context = new SuiteContext(serviceLoader);
      context.add(Configuration.class, configuration);
      
      ContainerCreator handler = new ContainerCreator();
      handler.callback(context, new SuiteEvent());
      
      Mockito.verify(container).setup(configuration);
      
      Assert.assertNotNull(
            "Should have exported " + DeployableContainer.class,
            context.get(DeployableContainer.class));
   }
}
