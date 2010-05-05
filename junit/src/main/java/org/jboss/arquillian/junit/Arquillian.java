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
package org.jboss.arquillian.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.impl.XmlConfigurationBuilder;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunnerAdaptor;
import org.jboss.arquillian.spi.util.TestEnrichers;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Arquillian
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class Arquillian extends BlockJUnit4ClassRunner
{
   private static ThreadLocal<TestRunnerAdaptor> deployableTest = new ThreadLocal<TestRunnerAdaptor>();
   
   public Arquillian(Class<?> klass) throws InitializationError
   {
      super(klass);
      if(deployableTest.get() == null) 
      {
         Configuration configuration = new XmlConfigurationBuilder().build();
         deployableTest.set(DeployableTestBuilder.build(configuration));
         try 
         {
            deployableTest.get().beforeSuite();
         } 
         catch (Exception e) 
         {
            throw new InitializationError(Arrays.asList((Throwable)e));
         }
      }
   }
   
   @Override
   public void run(RunNotifier notifier)
   {
      // register to listen for RunFinished to exeucte AfterSuite
      notifier.addListener(new RunListener() 
      {
         @Override
         public void testRunFinished(Result result) throws Exception
         {
            try  
            {
               if(deployableTest.get() != null) 
               {
                  deployableTest.get().afterSuite();
               }
            } 
            catch (Exception e) 
            {
               throw new RuntimeException("Could not stop container", e);
            }
         }
      });
      super.run(notifier);
   }

   @Override
   // TODO: exclude @Integration test classes
   protected List<FrameworkMethod> computeTestMethods()
   {
      return super.computeTestMethods();
   }


   /**
    * Override to allow test methods with arguments
    */
   @Override
   protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors)
   {
      List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
      for (FrameworkMethod eachTestMethod : methods)
      {
         eachTestMethod.validatePublicVoid(isStatic, errors);
      }
   }
      
   @Override
   protected Statement withBeforeClasses(final Statement originalStatement)
   {
      final Statement statementWithBefores = super.withBeforeClasses(originalStatement);
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            deployableTest.get().beforeClass(Arquillian.this.getTestClass().getJavaClass());
            statementWithBefores.evaluate();
         }
      };
   }
   
   @Override
   protected Statement withAfterClasses(final Statement originalStatement)
   {
      final Statement statementWithAfters = super.withAfterClasses(originalStatement);
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            statementWithAfters.evaluate();
            deployableTest.get().afterClass(Arquillian.this.getTestClass().getJavaClass());
         }
      };
   }
   
   @Override
   protected Statement withBefores(final FrameworkMethod method, final Object target, final Statement originalStatement)
   {
      final Statement statementWithBefores = super.withBefores(method, target, originalStatement);
      return new Statement()
      {
         @Override
         public void evaluate() throws Throwable
         {
            deployableTest.get().before(target, method.getMethod());
            statementWithBefores.evaluate();
         }
      };
   }
   
   @Override
   protected Statement withAfters(final FrameworkMethod method, final Object target, final Statement originalStatement)
   {
      final Statement statementWithAfters = super.withAfters(method, target, originalStatement);
      return new Statement()
      {
         @Override
         public void evaluate() throws Throwable
         {
            statementWithAfters.evaluate();
            deployableTest.get().after(target, method.getMethod());
         }
      };
   }
   
   @Override
   protected Statement methodInvoker(final FrameworkMethod method, final Object test)
   {
      return new Statement()
      {
         @Override
         public void evaluate() throws Throwable
         {
            TestResult result = deployableTest.get().test(new TestMethodExecutor()
            {
               public void invoke() throws Throwable
               {
                  Object parameterValues = TestEnrichers.enrich(deployableTest.get().getActiveContext(), getMethod());
                  method.invokeExplosively(test, (Object[])parameterValues);
               }
               
               public Method getMethod()
               {
                  return method.getMethod();
               }
               
               public Object getInstance()
               {
                  return test;
               }
            });
            if(result.getThrowable() != null)
            {
               throw result.getThrowable();
            }
         }
      };
   }
}
