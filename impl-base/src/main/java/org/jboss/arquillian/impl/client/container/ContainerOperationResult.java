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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;

/**
 * OperationResult
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerOperationResult<T>
{
   public enum Type
   {
      SETUP, START, STOP, DEPLOY, UNDEPLOY
   }
   
   private List<Operation<T>> operations;
   
   public ContainerOperationResult()
   {
      operations = new ArrayList<Operation<T>>();
   }
   
   void add(Operation<T> operation)
   {
      operations.add(operation);
   }
   
   public List<Operation<T>> getOperations()
   {
      return Collections.unmodifiableList(operations);
   }

   public boolean hasFailure() 
   {
      for(Operation<T> operation : operations)
      {
         if(operation.hasFailed())
         {
            return true;
         }
      }
      return false;
   }
   
   public interface Operation<T>
   {
      Type getType();
      
      Container getContainer();
   
      boolean hasFailed();
      
      Throwable getFailure();
      
      T getObject();
   }
   
   public static class GenericOperation<X> implements Operation<X>
   {
      private Type type;
      private Container contianer;
      private Throwable exception;
      private X object;
      
      public GenericOperation(Type type, Container contianer)
      {
         this(type, contianer, null, null);
      }

      public GenericOperation(Type type, Container contianer, X object)
      {
         this(type, contianer, null, object);
      }

      public GenericOperation(Type type, Container contianer, Throwable exception)
      {
         this(type, contianer, exception, null);
      }

      public GenericOperation(Type type, Container contianer, Throwable exception, X object)
      {
         this.type = type;
         this.contianer = contianer;
         this.exception = exception;
         this.object = object;
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.impl.container.ContainerOperationResult.Operation#getContainer()
       */
      public Container getContainer()
      {
         return contianer;
      }

      /* (non-Javadoc)
       * @see org.jboss.arquillian.impl.container.ContainerOperationResult.Operation#hasFailed()
       */
      public boolean hasFailed()
      {
         return getFailure() != null;
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.impl.container.ContainerOperationResult.Operation#getFailure()
       */
      public Throwable getFailure()
      {
         return exception;
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.impl.container.ContainerOperationResult.Operation#getObject()
       */
      public X getObject()
      {
         return object;
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.impl.container.ContainerOperationResult.Operation#getType()
       */
      public Type getType()
      {
         return type;
      }
   }
   
   public static Operation<Void> setupSuccess(Container container)
   {
      return new GenericOperation<Void>(Type.SETUP, container);
   }

   public static Operation<Void> setupFailure(Container container, Throwable exception)
   {
      return new GenericOperation<Void>(Type.SETUP, container, exception);
   }

   public static Operation<Void> startSuccess(Container container)
   {
      return new GenericOperation<Void>(Type.START, container);
   }

   public static Operation<Void> startFailure(Container container, Throwable exception)
   {
      return new GenericOperation<Void>(Type.START, container, exception);
   }

   public static Operation<Void> stopSuccess(Container container)
   {
      return new GenericOperation<Void>(Type.STOP, container);
   }

   public static Operation<Void> stopFailure(Container container, Throwable exception)
   {
      return new GenericOperation<Void>(Type.STOP, container, exception);
   }
   
   public static Operation<DeploymentDescription> deploySuccess(Container container, DeploymentDescription deployment)
   {
      return new GenericOperation<DeploymentDescription>(Type.DEPLOY, container, deployment);
   }

   public static Operation<DeploymentDescription> deployFailure(Container container, DeploymentDescription deployment, Throwable exception)
   {
      return new GenericOperation<DeploymentDescription>(Type.DEPLOY, container, exception, deployment);
   }

   public static Operation<DeploymentDescription> unDeploySuccess(Container container, DeploymentDescription deployment)
   {
      return new GenericOperation<DeploymentDescription>(Type.UNDEPLOY, container, deployment);
   }

   public static Operation<DeploymentDescription> unDeployFailure(Container container, DeploymentDescription deployment, Throwable exception)
   {
      return new GenericOperation<DeploymentDescription>(Type.UNDEPLOY, container, exception, deployment);
   }
}
