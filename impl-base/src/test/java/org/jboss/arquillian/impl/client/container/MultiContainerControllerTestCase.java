/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.impl.client.container;

import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * MultiContainerControllerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiContainerControllerTestCase
{
   @Mock
   private ServiceLoader loader;
   
   @Mock
   private ContainerRegistry manager;
   
   @Mock
   private Container container;
   
   @Mock
   private DeployableContainer deployableContainer;

   @Mock
   private DeployableContainer failingDeployableContainer;

   @Test
   @Ignore // not used
   public void shouldBeAbleToSetupMultipleContainers() throws Exception
   {
      /*
      Mockito.when(manager.getContainers()).thenReturn(Arrays.asList(container, container));
      Mockito.when(container.getDeployableContainer()).thenReturn(deployableContainer);
      
      Configuration configuration = new Configuration();
      SuiteContext context = new SuiteContext(loader);
      context.add(ContainerRegistry.class, manager);
      
      MultiContainerController controller = new MultiContainerController();
      ContainerOperationResult<Void> result = controller.setup(context, configuration);
      
      Assert.assertEquals(
            "Verify correct number of operations returned", 
            2, result.getOperations().size());
      
      for(Operation<Void> operation : result.getOperations())
      {
         Assert.assertTrue(
               "Verify correct operation type",
               operation.getType() == Type.SETUP);
         Assert.assertFalse(
               "Verify operation did not fail",
               operation.hasFailed());
      }
      
      Assert.assertFalse(
            "Verify no failure", 
            result.hasFailure());
      
      Mockito.verify(deployableContainer, Mockito.times(2)).setup(context, configuration);
      */
   }

   @Test
   @Ignore // not used
   public void shouldBeAbleToSetupMultipleContainersWithFailure() throws Exception
   {
      /*
      Exception e = new RuntimeException();
      Mockito.doThrow(e).when(failingDeployableContainer).setup(Mockito.any(Context.class), Mockito.any(Configuration.class));
               
               
      Mockito.when(manager.getContainers()).thenReturn(Arrays.asList(container, container));
      Mockito.when(container.getDeployableContainer()).thenReturn(failingDeployableContainer, deployableContainer);
      
      Configuration configuration = new Configuration();
      SuiteContext context = new SuiteContext(loader);
      context.add(ContainerRegistry.class, manager);
      
      MultiContainerController controller = new MultiContainerController();
      ContainerOperationResult<Void> result = controller.setup(context, configuration);
      
      Assert.assertEquals(
            "Verify correct number of operations returned", 
            2, result.getOperations().size());

      Assert.assertTrue(
            "Verify has failure", 
            result.hasFailure());
      
      // verify first operation
      Operation<Void> operation = result.getOperations().get(0);
      Assert.assertTrue(
            "Verify correct operation type",
            operation.getType() == Type.SETUP);
      Assert.assertTrue(
            "Verify operation did not fail",
            operation.hasFailed());

      // verify second operation
      operation = result.getOperations().get(1);
      Assert.assertTrue(
            "Verify correct operation type",
            operation.getType() == Type.SETUP);
      Assert.assertFalse(
            "Verify operation did not fail",
            operation.hasFailed());
      
      Mockito.verify(deployableContainer, Mockito.times(1)).setup(context, configuration);
      */
   }
}
