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
package org.jboss.arquillian.spi.client.deployment;

import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * Deployment
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentDescription
{
   private String name;
   private boolean managed = true;
   private int order = 0;
   private boolean testable = true;
   
   private TargetDescription target = TargetDescription.DEFAULT;
   private ProtocolDescription protocol= ProtocolDescription.DEFAULT;
   private Archive<?> archive;
   private Descriptor descriptor;
   
   private Archive<?> testableArchive;
   
   private Class<? extends Exception> expectedException;

   public DeploymentDescription(String name, Archive<?> archive)
   {
      this(name);
      
      Validate.notNull(archive, "Archive must be specified");
      this.archive = archive;
   }

   public DeploymentDescription(String name, Descriptor descriptor)
   {
      this(name);

      Validate.notNull(descriptor, "Descriptor must be specified");
      this.descriptor = descriptor;
   }

   private DeploymentDescription(String name)
   {
      Validate.notNull(name, "Name must be specified");

      this.name = name;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * @param order the order to set
    */
   public DeploymentDescription setOrder(int order)
   {
      this.order = order;
      return this;
   }
   
   
   /**
    * @return the order
    */
   public int getOrder()
   {
      return order;
   }
   
   /**
    * @param target the target to set
    */
   public DeploymentDescription setTarget(TargetDescription target)
   {
      Validate.notNull(target, "TargetDescription must be specified");
      this.target = target;
      return this;
   }
   
   /**
    * @return the target
    */
   public TargetDescription getTarget()
   {
      return target;
   }
   
   /**
    * @param protocol the protocol to set
    */
   public DeploymentDescription setProtocol(ProtocolDescription protocol)
   {
      this.protocol = protocol;
      return this;
   }

   /**
    * @return the protocol
    */
   public ProtocolDescription getProtocol()
   {
      return protocol;
   }

   public DeploymentDescription shouldBeManaged(boolean startup)
   {
      this.managed = startup;
      return this;
   }
   
   /**
    * @return the startup
    */
   public boolean managed()
   {
      return managed;
   }

   /**
    * @param testable the testable to set
    */
   public DeploymentDescription shouldBeTestable(boolean testable)
   {
      if(!isArchiveDeployment())
      {
         throw new IllegalArgumentException("A non ArchiveDeployment can not be testable: " + getName());
      }
      this.testable = testable;
      return this;
   }
   
   /**
    * @return the testable
    */
   public boolean testable()
   {
      return testable;
   }


   /**
    * @return the testableArchive
    */
   public Archive<?> getTestableArchive()
   {
      return testableArchive;
   }
   
   /**
    * @param testableArchive the testableArchive to set
    */
   public DeploymentDescription setTestableArchive(Archive<?> testableArchive)
   {
      this.testableArchive = testableArchive;
      return this;
   }

   /**
    * @return the expectedException
    */
   public Class<? extends Exception> getExpectedException()
   {
      return expectedException;
   }
   
   /**
    * @param expectedException the expectedException to set
    */
   public DeploymentDescription setExpectedException(Class<? extends Exception> expectedException)
   {
      this.expectedException = expectedException;
      return this;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - Deployment ----------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the archive
    */
   public Archive<?> getArchive()
   {
      return archive;
   }
   
   /**
    * @return the descriptor
    */
   public Descriptor getDescriptor()
   {
      return descriptor;
   }
   
   public boolean isDescriptorDeployment()
   {
      return descriptor != null;
   }
   
   public boolean isArchiveDeployment()
   {
      return archive != null;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return getName();
   }
}
