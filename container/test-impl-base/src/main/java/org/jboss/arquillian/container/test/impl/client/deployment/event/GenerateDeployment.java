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
package org.jboss.arquillian.container.test.impl.client.deployment.event;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.spi.event.Event;
import org.jboss.arquillian.test.spi.TestClass;

/**
 * GenerateDeployment
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class GenerateDeployment implements Event
{
   private TestClass testClass;
   private InstanceProducer<DeploymentScenario> producer;

   public GenerateDeployment(TestClass testClass)
   {
      this(testClass, null);
   }

   public GenerateDeployment(TestClass testClass, InstanceProducer<DeploymentScenario> producer)
   {
      this.testClass = testClass;
      this.producer = producer;
   }

   /**
    * @return the testClass
    */
   public TestClass getTestClass()
   {
      return testClass;
   }

   public void set(DeploymentScenario scenario) {
       producer.set(scenario);
   }

   public boolean hasProducer() {
       return producer != null;
   }
}
