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
package org.jboss.arquillian.impl.context;

import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ServiceLoader;


/**
 * Handles creation and destruction of the scopes; Suite, Class, Test <br/>
 * There can only be one SuiteContext pr run. A SuiteContext can be associated with multiple ClassContexts that 
 * again can be associated with multiple TestContexts.<br/>
 * <br/>
 * Normal Lifecycle:<br/>
 * {@link #createRestoreSuiteContext()}<br/>
 * <br/>
 * {@link #createRestoreClassContext(Class)}<br/>
 * ...<br/>
 * {@link #createRestoreTestContext(Object)}<br/>
 * {@link #destroyTestContext(Object)}<br/>
 * ...<br/>
 * {@link #destroyClassContext(Class)}<br/>
 *  <br/>
 * {@link #destroySuiteContext()}<br/>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContextLifecycleManager
{
   private Configuration configuration;
   private ProfileBuilder profileBuilder;
   private ServiceLoader serviceLoader;
   
   private SuiteContext suiteContext;
   
   // TODO: move out into a ContextStore IF
   private ConcurrentHashMap<Class<?>, ClassContext> classContextStore;
   private ConcurrentHashMap<Object, TestContext> testContextStore;
   
   public ContextLifecycleManager(ProfileBuilder profileBuilder, ServiceLoader serviceLoader) 
   {
      this(new Configuration(), profileBuilder, serviceLoader);
   }
   
   public ContextLifecycleManager(Configuration configuration, ProfileBuilder profileBuilder, ServiceLoader serviceLoader)
   {
      Validate.notNull(configuration, "Configuration must be specified");
      Validate.notNull(profileBuilder, "ProfileBuilder must be specified");
      Validate.notNull(serviceLoader, "ServiceLoader must be specified");
      
      this.configuration = configuration;
      this.profileBuilder = profileBuilder;
      this.serviceLoader = serviceLoader;
      
      classContextStore = new ConcurrentHashMap<Class<?>, ClassContext>();
      testContextStore = new ConcurrentHashMap<Object, TestContext>();  
   }

   /**
    * Creates or restores the SuiteContext.<br/>
    * 
    * If the Context has previously been created, the same instance will be returned. 
    * 
    * @return A SuiteContext instance
    */
   public SuiteContext createRestoreSuiteContext() 
   {
      if(suiteContext == null) 
      {
         suiteContext = new SuiteContext(serviceLoader);
         suiteContext.add(Configuration.class, configuration);
         profileBuilder.buildSuiteContext(suiteContext);
      }
      return suiteContext;
   }
   
   /**
    * Destroy the Context.<br/>
    * 
    * A new Context instance will be returned on next {@link #createRestoreSuiteContext()} call.
    *  
    * @param testClass Context association
    */
   public void destroySuiteContext() 
   {
      suiteContext = null;
   }
   
   /**
    * Creates or restores the ClassContext.<br/>
    * 
    * If the Context has previously been created, the same instance will be returned. 
    * 
    * @param testClass The TestClass this Context belongs to
    * @return A ClassContext instance
    * @throws IllegalArgumentException if testClass is null
    * @throws IllegalStateException if {@link #createRestoreSuiteContext()} has not been called
    */
   public ClassContext createRestoreClassContext(Class<?> testClass)
   {
      Validate.notNull(testClass, "TestClass must be specified");
      
      if(suiteContext == null)
      {
         throw new IllegalStateException(
               "No " + 
               SuiteContext.class.getSimpleName() + 
               " found, please create one before creating a " + 
               ClassContext.class.getSimpleName());
      }
      
      if(!classContextStore.contains(testClass)) 
      {
         ClassContext classContext = new ClassContext(createRestoreSuiteContext()); 
         profileBuilder.buildClassContext(classContext, testClass);
         
         classContextStore.putIfAbsent(
               testClass, 
               classContext);
      }
      return classContextStore.get(testClass);
   }
   
   /**
    * Destroy the Context associated with given test class.<br/>
    * 
    * A new Context instance will be returned on next {@link #createRestoreClassContext(Class)} call.
    *  
    * @param testClass Context association
    */
   public void destroyClassContext(Class<?> testClass)
   {
      classContextStore.remove(testClass);
   }
   
   /**
    * Creates or restores the TestContext.<br/>
    * 
    * If the Context has previously been created, the same instance will be returned. 
    * 
    * @param testInstance The TestObject this Context belongs to
    * @return A TestContext instance
    * @throws IllegalArgumentException if testInstance is null
    */
   // TODO: split create and restore in two private methods so we can verify if previous context was created. 
   // Expose the createRestore v.
   public TestContext createRestoreTestContext(Object testInstance)
   {
      Validate.notNull(testInstance, "TestInstance must be specified");
      
      if(!testContextStore.contains(testInstance)) 
      {
         TestContext testContext = new TestContext(createRestoreClassContext(testInstance.getClass()));
         profileBuilder.buildTestContext(testContext, testInstance);
         testContextStore.putIfAbsent(
               testInstance, 
               testContext);
      }
      return testContextStore.get(testInstance);
   }
   
   /**
    * Destroy the Context associated with given test instance.<br/>
    * 
    * A new Context instance will be returned on next {@link #createRestoreTestContext(Object)} call.
    *  
    * @param testInstance Context association
    */
   public void destroyTestContext(Object testInstance)
   {
      testContextStore.remove(testInstance);
   }
}
