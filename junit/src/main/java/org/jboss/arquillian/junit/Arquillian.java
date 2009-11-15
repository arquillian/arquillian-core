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

import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.api.TestMethodExecutor;
import org.jboss.arquillian.impl.DeployableTest;
import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.impl.runner.servlet.InContainerListener;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.descriptor.WebArchiveDescriptor;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

// TODO: where to put start/stop container..
/**
 * Arquillian
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class Arquillian extends BlockJUnit4ClassRunner
{
   private static DeployableTest deployableTest;
   
   private Archive<?> archive = null;
   
   public Arquillian(Class<?> klass) throws InitializationError
   {
      super(klass);
      if(deployableTest == null) 
      {
         deployableTest = DeployableTestBuilder.build(null);
         try 
         {
            deployableTest.getContainerController().start();
         } 
         catch (Exception e) 
         {
            throw new InitializationError(Arrays.asList((Throwable)e));
         }
      }
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run()
         {
            try  
            {
               deployableTest.getContainerController().stop();
            } 
            catch (Exception e) 
            {
               // TODO: stops container, but complains about wrong state ? 
               //throw new RuntimeException("Could not stop contianer", e);
            }
         }
      });
   }


   @Override
   // TODO: exclude @Integration test classes
   protected List<FrameworkMethod> computeTestMethods()
   {
      return super.computeTestMethods();
   }

   @Override
   protected Statement withBeforeClasses(Statement statement)
   {
      final Statement originalStatement = super.withBeforeClasses(statement);
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            archive = deployableTest.generateArchive(
                  Arquillian.this.getTestClass().getJavaClass());
            
            if(archive instanceof WebArchive) {
               WebArchive webArchive = (WebArchive)archive;
               webArchive.addPackages(
                     true,
                     Package.getPackage("org.junit"),
                     Package.getPackage("org.jboss.arquillian.api"), 
                     Package.getPackage("org.jboss.arquillian.impl"),
                     Package.getPackage("org.jboss.arquillian.junit"));
               
               webArchive.as(WebArchiveDescriptor.class)
                  .addListener(InContainerListener.class)
                  .addServlet(ServletTestRunner.class, "/*");
            }
            if(archive instanceof JavaArchive) {
               EnterpriseArchive ear = Archives.create("test.ear", EnterpriseArchive.class);

               WebArchive war = Archives.create("test.war", WebArchive.class)
                     .addPackages(
                        true,
                        Package.getPackage("org.junit"),
                        Package.getPackage("org.jboss.arquillian.api"), 
                        Package.getPackage("org.jboss.arquillian.impl"),
                        Package.getPackage("org.jboss.arquillian.junit"))
                     .addClass(Arquillian.this.getTestClass().getJavaClass());
               
               war.as(WebArchiveDescriptor.class)
                  .addListener(InContainerListener.class)
                  .addServlet(ServletTestRunner.class, "/*");
                  
               ear.addModule(war)
                  .addModule(archive);
             
               archive = ear;
            }

            deployableTest.getDeployer().deploy(archive);
            originalStatement.evaluate();
         }
      };
   }
   
   @Override
   protected Statement withAfterClasses(Statement statement)
   {
      final Statement originalStatement = super.withAfterClasses(statement);
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            originalStatement.evaluate();
            deployableTest.getDeployer().undeploy(archive);
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
            deployableTest.run(new TestMethodExecutor()
            {
               public void invoke() throws Throwable
               {
                  method.invokeExplosively(test);
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
         }
      };
   }
}
