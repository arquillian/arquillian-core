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
package org.jboss.arquillian.impl.enricher;

import java.util.Collection;

import org.jboss.arquillian.api.DeploymentTarget;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.test.DeploymentTargetDescription;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.Before;

/**
 * A Handler for enriching the Test instance.<br/>
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClientTestEnricher 
{
   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   @Inject
   private Instance<ContainerRegistry> containerRegistry;

   @Inject
   private Instance<ContainerContext> containerContextProvider;

   @Inject
   private Instance<DeploymentContext> deploymentContextProvider;

   @Inject
   private Instance<ServiceLoader> serviceLoader;
   
   @Inject
   private Instance<Injector> injector;

   public void enrich(@Observes Before event) throws Exception
   {
      ContainerContext containerContext = containerContextProvider.get();
      DeploymentContext deploymentContext = deploymentContextProvider.get();

      // TODO : move as a abstract/SPI on TestMethodExecutor
      DeploymentTargetDescription target = null;
      if(event.getTestMethod().isAnnotationPresent(DeploymentTarget.class))
      {
         target = new DeploymentTargetDescription(event.getTestMethod().getAnnotation(DeploymentTarget.class).value());
      }
      else
      {
         target = DeploymentTargetDescription.DEFAULT;
      }
      
      DeploymentScenario scenario = deploymentScenario.get();
      
      try
      {
         DeploymentDescription deployment = scenario.getDeployment(target);

         Container container = containerRegistry.get().getContainer(deployment.getTarget());
         containerContext.activate(container.getName());

         try
         {
            deploymentContext.activate(deployment);

            Collection<TestEnricher> testEnrichers = serviceLoader.get().all(container.getClassLoader(), TestEnricher.class);
            for(TestEnricher enricher : testEnrichers) 
            {
               injector.get().inject(enricher);
               enricher.enrich(event.getTestInstance());
            }
         }
         finally
         {
            deploymentContext.deactivate();
         }
      }
      finally 
      {
         containerContext.deactivate();
      }
   }
}
