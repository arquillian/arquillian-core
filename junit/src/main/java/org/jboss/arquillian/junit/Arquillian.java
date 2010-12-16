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
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunnerAdaptor;
import org.junit.internal.runners.model.MultipleFailureException;
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
   /*
    * @HACK
    * JUnit Hack:
    * In JUnit a Exception is thrown and verified/swallowed if @Test(expected) is set. We need to transfer this
    * Exception back to the client so the client side can throw it again. This to avoid a incontainer working but failing
    * on client side due to no Exception thrown. 
    */
   // Cleaned up in JUnitTestRunner
   public static ThreadLocal<Throwable> caughtTestException = new ThreadLocal<Throwable>();

   /*
    * @HACK
    * Eclipse hack:
    * When running multiple TestCases, Eclipse will create a new runner for each of them.
    * This results in that AfterSuite is call pr TestCase, but BeforeSuite only on the first created instance.
    * A instance of all TestCases are created before the first one is started, so we keep track of which one 
    * was the last one created. The last one created is the only one allowed to call AfterSuite.
    */
   private static ThreadLocal<Arquillian> lastCreatedRunner = new ThreadLocal<Arquillian>();

   private static ThreadLocal<TestRunnerAdaptor> deployableTest = new ThreadLocal<TestRunnerAdaptor>();
   
   public Arquillian(Class<?> klass) throws InitializationError
   {
      super(klass);
      try
      {
         // first time we're being initialized
         if(deployableTest.get() == null)   
         {
            // no, initialization has been attempted before, refuse to do anything else
            if(lastCreatedRunner.get() != null)  
            {
                throw new RuntimeException("Arquillian has previously been attempted initialized, but failed. See previous exceptions for cause.");
            }
            TestRunnerAdaptor adaptor = DeployableTestBuilder.build();
            try 
            {
               // don't set it if beforeSuite fails
               adaptor.beforeSuite();
               deployableTest.set(adaptor);
            } 
            catch (Exception e) 
            {
               throw new InitializationError(Arrays.asList((Throwable)e));
            }
         }
      }
      finally 
      {
         lastCreatedRunner.set(this);
      }
   }
   
   @Override
   public void run(RunNotifier notifier)
   {
      notifier.addListener(new RunListener() 
      {
         @Override
         public void testRunFinished(Result result) throws Exception
         {
            try  
            {
               if(deployableTest.get() != null && lastCreatedRunner.get() == Arquillian.this) 
               {
                  deployableTest.get().afterSuite();
                  deployableTest.get().shutdown();
                  lastCreatedRunner.set(null);
                  lastCreatedRunner.remove();
                  deployableTest.set(null);
                  deployableTest.remove();
               }
            } 
            catch (Exception e) 
            {
               throw new RuntimeException("Could not run @AfterSuite", e);
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
            try
            {
               deployableTest.get().beforeClass(Arquillian.this.getTestClass().getJavaClass());
               statementWithBefores.evaluate();
            } 
            catch (Exception e) // catch and rethrow only to be able to set a break point. 
            {
               throw e;
            }
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
            new MultiStatementExecutor().execute
            (
                  new Statement() { public void evaluate() throws Throwable
                  {
                     statementWithAfters.evaluate();
                  }},
                  new Statement() { public void evaluate() throws Throwable 
                  {
                     deployableTest.get().afterClass(Arquillian.this.getTestClass().getJavaClass());
                  }}
            );
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
            new MultiStatementExecutor().execute
            (
                  new Statement() { public void evaluate() throws Throwable 
                  {
                     statementWithAfters.evaluate();
                  }},
                  new Statement() { public void evaluate() throws Throwable 
                  {
                     deployableTest.get().after(target, method.getMethod());
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
            TestResult result = deployableTest.get().test(new TestMethodExecutor()
            {
               public void invoke() throws Throwable
               {
                  try
                  {
                     //Object parameterValues = TestEnrichers.enrich(deployableTest.get().getActiveContext(), getMethod());
                     method.invokeExplosively(test); //, (Object[])parameterValues);
                  } 
                  catch (Throwable e) 
                  {
                     // Force a way to return the thrown Exception from the Container the client. 
                     caughtTestException.set(e);
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
            if(result.getThrowable() != null)
            {
               throw result.getThrowable();
            }
         }
      };
   }

   /**
    * A helper class to safely execute multiple statements in one.<br/>
    * 
    * Will execute all statements even if they fail, all exceptions will be kept. If multiple {@link Statement}s
    * fail, a {@link MultipleFailureException} will be thrown.
    *
    * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
    * @version $Revision: $
    */
   private class MultiStatementExecutor 
   {
      public void execute(Statement... statements) throws Throwable 
      {
         List<Throwable> exceptions = new ArrayList<Throwable>();
         for(Statement command : statements) 
         {
            try
            {
               command.evaluate();
            } 
            catch (Exception e) 
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
   }
}