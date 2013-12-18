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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Main Arquillian JUnit runner
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class Arquillian extends BlockJUnit4ClassRunner
{
   public Arquillian(Class<?> klass) throws InitializationError
   {
      super(klass);
      State.runnerStarted();
   }
   
   @Override
   protected List<FrameworkMethod> getChildren()
   {
      List<FrameworkMethod> children = super.getChildren();
      // Only sort if InOrder is defined, else keep them in original order returned by parent
      boolean hasDefinedOrder = false;
      for(FrameworkMethod method : children)
      {
         if(method.getAnnotation(InSequence.class) != null)
         {
            hasDefinedOrder = true;
         }
      }
      if(hasDefinedOrder)
      {
         Collections.sort(children, new InSequenceSorter());
      }
      return children;
   }

   @Override
   public void run(final RunNotifier notifier)
   {
      // first time we're being initialized
      if(!State.hasTestAdaptor())   
      {
         // no, initialization has been attempted before and failed, refuse to do anything else
         if(State.hasInitializationException())  
         {
            // failed on suite level, ignore children
            //notifier.fireTestIgnored(getDescription());
            notifier.fireTestFailure(
                  new Failure(getDescription(), 
                        new RuntimeException(
                              "Arquillian has previously been attempted initialized, but failed. See cause for previous exception", 
                              State.getInitializationException())));
         }
         else
         {
            TestRunnerAdaptor adaptor = TestRunnerAdaptorBuilder.build();
            try 
            {
               // don't set it if beforeSuite fails
               adaptor.beforeSuite();
               State.testAdaptor(adaptor);
            } 
            catch (Exception e)  
            {
               // caught exception during BeforeSuite, mark this as failed
               State.caughtInitializationException(e);
               notifier.fireTestFailure(new Failure(getDescription(), e));
            }
         }
      }
      notifier.addListener(new RunListener()
      {
         @Override
         public void testRunFinished(Result result) throws Exception
         {
            State.runnerFinished();
            shutdown();
         }

         private void shutdown()
         {
            try
            {
               if(State.isLastRunner())
               {
                  try
                  {
                     if(State.hasTestAdaptor())
                     {
                        TestRunnerAdaptor adaptor = State.getTestAdaptor();
                        adaptor.afterSuite();
                        adaptor.shutdown();
                     }
                  }
                  finally
                  {
                     State.clean();
                  }
               }
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not run @AfterSuite", e);
            }
         }
      });
      // initialization ok, run children
      if(State.hasTestAdaptor())
      {
         super.run(notifier);
      }
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
      
   /*
    * Override BeforeClass/AfterClass and Before/After handling.
    * 
    * Let super create the Before/After chain against a EmptyStatement so our newly created Statement
    * only contains the method that are of interest to us(@Before..etc). 
    * They can then optionally be executed if we get expected callback.
    * 
    */
      
   @Override
   protected Statement withBeforeClasses(final Statement originalStatement)
   {
      final Statement onlyBefores = super.withBeforeClasses(new EmptyStatement());
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            State.getTestAdaptor().beforeClass(
                  Arquillian.this.getTestClass().getJavaClass(), 
                  new StatementLifecycleExecutor(onlyBefores));
            originalStatement.evaluate();
         }
      };
   }
   
   @Override
   protected Statement withAfterClasses(final Statement originalStatement)
   {
      final Statement onlyAfters = super.withAfterClasses(new EmptyStatement());
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            multiExecute
            (
               originalStatement,
               new Statement() { @Override public void evaluate() throws Throwable 
               {
                  State.getTestAdaptor().afterClass(
                        Arquillian.this.getTestClass().getJavaClass(), 
                        new StatementLifecycleExecutor(onlyAfters));
               }}
            );
         }
      };
   }   
   
   @Override
   protected Statement withBefores(final FrameworkMethod method, final Object target, final Statement originalStatement)
   {
      final Statement onlyBefores = super.withBefores(method, target, new EmptyStatement());
      return new Statement()
      {
         @Override
         public void evaluate() throws Throwable
         {
            State.getTestAdaptor().before(
                  target, 
                  method.getMethod(), 
                  new StatementLifecycleExecutor(onlyBefores));
            originalStatement.evaluate();
         }
      };
   }
   
   @Override
   protected Statement withAfters(final FrameworkMethod method, final Object target, final Statement originalStatement)
   {
      final Statement onlyAfters = super.withAfters(method, target, new EmptyStatement());
      return new Statement()
      {
         @Override
         public void evaluate() throws Throwable
         {
            multiExecute
            (
               originalStatement, 
               new Statement() { @Override public void evaluate() throws Throwable
               {
                  State.getTestAdaptor().after(
                        target, 
                        method.getMethod(), 
                        new StatementLifecycleExecutor(onlyAfters));
               }}
            );
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
            TestResult result = State.getTestAdaptor().test(new TestMethodExecutor()
            {
               @Override
               public void invoke(Object... parameters) throws Throwable
               {
                  try
                  {
                     method.invokeExplosively(test, parameters); 
                  } 
                  catch (Throwable e) 
                  {
                     // Force a way to return the thrown Exception from the Container the client. 
                     State.caughtTestException(e);
                     throw e;
                  }
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
            if (result != null && result.getThrowable() != null)
            {
               throw result.getThrowable();
            }
         }
      };
   }

   /**
    * A helper to safely execute multiple statements in one.<br/>
    * 
    * Will execute all statements even if they fail, all exceptions will be kept. If multiple {@link Statement}s
    * fail, a {@link MultipleFailureException} will be thrown.
    *
    * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
    * @version $Revision: $
    */
   private void multiExecute(Statement... statements) throws Throwable 
   {
      List<Throwable> exceptions = new ArrayList<Throwable>();
      for(Statement command : statements) 
      {
         try
         {
            command.evaluate();
         } 
         catch (Throwable e) 
         {
            exceptions.add(e);
         }
      }
      if(exceptions.isEmpty())
      {
         return;
      }
      if(exceptions.size() == 1)
      {
         throw exceptions.get(0);
      }
      throw new MultipleFailureException(exceptions);
   }
   
   private static class EmptyStatement extends Statement
   {
      @Override
      public void evaluate() throws Throwable
      {
      }
   }
 
   private static class StatementLifecycleExecutor implements LifecycleMethodExecutor
   {
      private Statement statement;
      
      public StatementLifecycleExecutor(Statement statement)
      {
         this.statement = statement;
      }
      
      public void invoke() throws Throwable
      {
         statement.evaluate();
      }
   }
}