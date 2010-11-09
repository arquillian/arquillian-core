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
package org.jboss.arquillian.impl.core;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.impl.core.context.ApplicationContextImpl;
import org.jboss.arquillian.impl.core.spi.EventPoint;
import org.jboss.arquillian.impl.core.spi.Extension;
import org.jboss.arquillian.impl.core.spi.InjectionPoint;
import org.jboss.arquillian.impl.core.spi.Manager;
import org.jboss.arquillian.impl.core.spi.ObserverMethod;
import org.jboss.arquillian.impl.core.spi.context.ApplicationContext;
import org.jboss.arquillian.impl.core.spi.context.Context;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.event.ManagerStarted;

/**
 * ManagerImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ManagerImpl implements Manager
{
   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private List<Context> contexts;
   private List<Extension> extensions;
   

   ManagerImpl(List<Class<? extends Context>> contextClasses, List<Class<?>> extensionClasses)
   {
      this.contexts = new ArrayList<Context>();
      this.extensions = new ArrayList<Extension>();
      try
      {
         
         List<Extension> createdExtensions = createExtensions(extensionClasses);
         List<Context> createdContexts = createContexts(contextClasses);
         
         createApplicationContextAndActivate();
         
         this.contexts.addAll(createdContexts);
         this.extensions.addAll(createdExtensions);
         
         addContextsToApplicationScope();
         fire(new ManagerStarted());
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not create and startup manager", e);
      }
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations - Manager -------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Override
   public void fire(Object event)
   {
      Validate.notNull(event, "Event must be specified");
      List<ObserverMethod> observers = resolveObservers(event.getClass());
      for(ObserverMethod observer : observers)
      {
         observer.invoke(event);
      }
   }

   @Override
   public <T> void bind(Class<? extends Annotation> scope, Class<T> type, T instance) 
   {
      Validate.notNull(scope, "Scope must be specified");
      Validate.notNull(type, "Type must be specified");
      Validate.notNull(instance, "Instance must be specified");

      Context scopedContext = getScopedContext(scope);
      if(scopedContext == null)
      {
         throw new IllegalArgumentException("No Context registered with support for scope: " + scope);
      }
      if(!scopedContext.isActive())
      {
         throw new IllegalArgumentException("No active " + scope.getSimpleName() + " Context to bind to");
      }
      scopedContext.getObjectStore().add(type, instance);
   }
   
   @Override
   public <T> T resolve(Class<T> type)
   {
      Validate.notNull(type, "Type must be specified");
      List<Context> activeContexts = resolveActiveContexts();
      for(int i = activeContexts.size() -1; i >= 0; i--)
      {
         Context context = activeContexts.get(i);
         T object = context.getObjectStore().get(type);
         if(object != null)
         {
            return object;
         }
      }
      return null;
   }

   @Override
   public void inject(Object obj)
   {
      inject(ExtensionImpl.of(obj));
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Manager#getContext(java.lang.Class)
    */
   @Override
   public <T> T getContext(Class<T> type)
   {
      for(Context context : contexts)
      {
         if(type.isInstance(context))
         {
            return type.cast(context);
         }
      }
      return null;
   }

   //-------------------------------------------------------------------------------------||
   // Exposed Convenience Impl Methods ---------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @param <T>
    * @param scope
    * @param type
    * @param instance
    */
   public <T> void bindAndFire(Class<? extends Annotation> scope, Class<T> type, T instance) 
   {
      bind(scope, type, instance);
      fire(instance);
   }
   
   /**
    * @return the extensions
    */
   public <T> T getExtension(Class<T> type)
   {
      for(Extension extension : extensions)
      {
         Object target = ((ExtensionImpl)extension).getTarget();
         if(type.isInstance(target))
         {
            return type.cast(target);
         }
      }
      return null;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.core.spi.Manager#shutdown()
    */
   @Override
   public void shutdown()
   {
      synchronized (this)
      {
         for(Context context : contexts)
         {
            context.clearAll();
         }
         contexts.clear();
         extensions.clear();
      }
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @param extensions
    * @return
    */
   private List<Extension> createExtensions(List<Class<?>> extensionClasses) throws Exception
   {
      List<Extension> created = new ArrayList<Extension>();
      for(Class<?> extensionClass : extensionClasses)
      {
         Extension extension = ExtensionImpl.of(Reflections.createInstance(extensionClass));
         inject(extension);
         created.add(extension);
      }
      return created;
   }

   /**
    * @param contexts2
    * @return
    */
   private List<Context> createContexts(List<Class<? extends Context>> contextClasses) throws Exception
   {
      List<Context> created = new ArrayList<Context>();
      for(Class<? extends Context> contextClass : contextClasses)
      {
         created.add(Reflections.createInstance(contextClass));
      }
      return created;
   }

   /**
    * 
    */
   private void createApplicationContextAndActivate()
   {    
      ApplicationContext context = new ApplicationContextImpl();
      context.activate();
      context.getObjectStore().add(Injector.class, InjectorImpl.of(this));
      contexts.add(context);
   }

   /**
    * @param objectStore
    */
   @SuppressWarnings("unchecked")
   private void addContextsToApplicationScope()
   {
      ApplicationContext appContext = getContext(ApplicationContext.class);
      ObjectStore store = appContext.getObjectStore();
      
      for(Context context : contexts)
      {
         store.add((Class<Context>)context.getClass().getInterfaces()[0], context);
      }
   }

   /**
    * @param eventType
    * @return
    */
   private List<ObserverMethod> resolveObservers(Class<?> eventType)
   {
      List<ObserverMethod> observers = new ArrayList<ObserverMethod>();
      for(Extension extension : extensions)
      {
         for(ObserverMethod observer : extension.getObservers())
         {
            if(observer.getType().isAssignableFrom(eventType))
            {
               observers.add(observer);
            }
         }
      }
      return observers;
   }

   private List<Context> resolveActiveContexts()
   {
      List<Context> activeContexts = new ArrayList<Context>();
      for(Context context : contexts)
      {
         if(context.isActive())
         {
            activeContexts.add(context);
         }
      }
      return activeContexts;
   }
   
   private void inject(Extension extension)
   {
      injectInstances(extension);
      injectEvents(extension);
   }

   /**
    * @param extension
    */
   private void injectInstances(Extension extension)
   {
      for(InjectionPoint point : extension.getInjectionPoints())
      {
         point.set(InstanceImpl.of(point.getType(), point.getScope(), this));
      }
   }
   
   /**
    * @param extension
    */
   private void injectEvents(Extension extension)
   {
      for(EventPoint point : extension.getEventPoints())
      {
         point.set(EventImpl.of(point.getType(), this));
      }
   }

   private Context getScopedContext(Class<? extends Annotation> scope)
   {
      for(Context context : contexts)
      {
         if(context.getScope() == scope)
         {
            return context;
         }
      }
      return null;
   }
}
